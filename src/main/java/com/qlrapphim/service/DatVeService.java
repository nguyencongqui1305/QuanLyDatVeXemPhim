package com.qlrapphim.service;

import com.qlrapphim.dto.DatVeRequestDTO;
import com.qlrapphim.dto.DatVeResponseDTO;
import com.qlrapphim.entity.*;
import com.qlrapphim.exception.*;
import com.qlrapphim.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatVeService {

    private final DatVeRepository datVeRepository;
    private final VeRepository veRepository;
    private final LichChieuRepository lichChieuRepository;
    private final GheRepository gheRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final SeatHoldService seatHoldService;

    public List<DatVe> findByKhachHang(String maKh) {
        return datVeRepository.findByKhachHangMaKhOrderByNgayDatDesc(maKh);
    }

    public Optional<DatVe> findById(String maDat) {
        return datVeRepository.findById(maDat);
    }

    /**
     * Xu ly toan bo quy trinh dat ve:
     * 1. Kiem tra ghe trong (FUNC_GHE_TRONG - tang service + tang DB unique constraint)
     * 2. Tao dat ve
     * 3. Tao tung ve cho moi ghe (PROC_THEM_VE)
     * 4. Tinh khuyen mai (neu co)
     * 5. Tao thanh toan
     * 6. Cap nhat trang thai dat ve (PROC_CAP_NHAT_THANH_TOAN)
     * 7. Giai phong ghe giu tam
     */
    @Transactional
    public DatVeResponseDTO datVe(DatVeRequestDTO request, String maKh, String maNv) {
        // 1. Load entities
        LichChieu lichChieu = lichChieuRepository.findById(request.getMaLich())
                .orElseThrow(() -> new ResourceNotFoundException("Lịch chiếu", "MA_LICH", request.getMaLich()));

        if (!"Mở bán".equals(lichChieu.getTrangThai())) {
            throw new BusinessException("Suất chiếu này hiện không còn mở bán");
        }

        KhachHang khachHang;
        if (maKh != null && !maKh.isBlank()) {
            khachHang = khachHangRepository.findById(maKh)
                    .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "MA_KH", maKh));
        } else if (maNv != null && !maNv.isBlank()) {
            // NhanVien/QuanLy đặt vé - tìm KH theo email NV, nếu ko có thì tạo mới
            NhanVien nv = nhanVienRepository.findById(maNv)
                    .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "MA_NV", maNv));
            khachHang = khachHangRepository.findByEmail(nv.getEmail()).orElse(null);
            if (khachHang == null) {
                long cnt = khachHangRepository.count() + 1;
                String newMaKh = String.format("KH%04d", cnt);
                while (khachHangRepository.existsById(newMaKh)) {
                    cnt++;
                    newMaKh = String.format("KH%04d", cnt);
                }
                khachHang = KhachHang.builder()
                        .maKh(newMaKh)
                        .hoTen(nv.getHoTen())
                        .email(nv.getEmail())
                        .sdt("0000000000")
                        .matKhau(nv.getMatKhau())
                        .ngayDangKy(java.time.LocalDate.now())
                        .build();
                khachHang = khachHangRepository.save(khachHang);
            }
        } else {
            throw new BusinessException("Không xác định được khách hàng đặt vé");
        }

        NhanVien nhanVien = null;
        if (maNv != null && !maNv.isBlank()) {
            nhanVien = nhanVienRepository.findById(maNv).orElse(null);
        }

        // Xu ly khuyen mai neu co
        KhuyenMai khuyenMai = null;
        if (request.getMaKm() != null && !request.getMaKm().isBlank()) {
            khuyenMai = khuyenMaiRepository.findById(request.getMaKm()).orElse(null);
            if (khuyenMai != null) {
                LocalDate today = LocalDate.now();
                boolean hieuLuc = (khuyenMai.getNgayBatDau() == null || !khuyenMai.getNgayBatDau().isAfter(today))
                        && (khuyenMai.getNgayKetThuc() == null || !khuyenMai.getNgayKetThuc().isBefore(today));
                if (!hieuLuc) {
                    khuyenMai = null; // Khuyen mai het han, bo qua
                }
            }
        }

        // 2. Tao DatVe
        String maDat = generateMaDat();
        DatVe datVe = DatVe.builder()
                .maDat(maDat)
                .khachHang(khachHang)
                .nhanVien(nhanVien)
                .khuyenMai(khuyenMai)
                .ngayDat(LocalDateTime.now())
                .tongTien(BigDecimal.ZERO)
                .trangThai("Chờ thanh toán")
                .build();
        datVe = datVeRepository.save(datVe);

        // 3. Tao tung ve cho moi ghe (PROC_THEM_VE)
        BigDecimal tongTienGoc = BigDecimal.ZERO;
        List<Ve> danhSachVe = new ArrayList<>();

        for (String maGhe : request.getMaGhes()) {
            Ghe ghe = gheRepository.findById(maGhe)
                    .orElseThrow(() -> new ResourceNotFoundException("Ghế", "MA_GHE", maGhe));

            // KIEM TRA CHONG DAT TRUNG GHE - Logic chinh (tang Service)
            // Ket hop voi UNIQUE constraint trong DB (tang DB) lam 2 lop bao ve
            Optional<Ve> veTonTai = veRepository.findVeDaDat(request.getMaLich(), maGhe);
            if (veTonTai.isPresent()) {
                // Rollback toan bo transaction
                throw new GheDaDatException(
                        String.format("Ghế %s%d đã được đặt. Vui lòng chọn ghế khác.",
                                ghe.getHangGhe(), ghe.getSoGhe()));
            }

            if (!"Hoạt động".equals(ghe.getTrangThai())) {
                throw new BusinessException(
                        String.format("Ghế %s%d đang bảo trì, không thể đặt.",
                                ghe.getHangGhe(), ghe.getSoGhe()));
            }

            BigDecimal giaVeGhe = tinhGiaVeTheoLoaiGhe(lichChieu.getGiaVe(), ghe.getLoaiGhe());

            String maVe = generateMaVe();
            Ve ve = Ve.builder()
                    .maVe(maVe)
                    .datVe(datVe)
                    .lichChieu(lichChieu)
                    .ghe(ghe)
                    .giaVe(giaVeGhe)
                    .trangThai("Đã đặt")
                    .build();
            veRepository.save(ve);
            danhSachVe.add(ve);

            // Cong tich luy gia ve vao tong tien (giong PROC_THEM_VE)
            tongTienGoc = tongTienGoc.add(giaVeGhe);
        }

        // 4. Tinh khuyen mai
        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (khuyenMai != null) {
            soTienGiam = tinhGiamGia(tongTienGoc, khuyenMai);
        }
        BigDecimal tongTienSauGiam = tongTienGoc.subtract(soTienGiam).max(BigDecimal.ZERO);

        // 5. Cap nhat tong tien vao DatVe
        datVe.setTongTien(tongTienSauGiam);

        // 6. Tao thanh toan va cap nhat trang thai (PROC_CAP_NHAT_THANH_TOAN)
        String maTt = generateMaTt();
        ThanhToan thanhToan = ThanhToan.builder()
                .maTt(maTt)
                .datVe(datVe)
                .soTien(tongTienSauGiam)
                .ngayTt(LocalDateTime.now())
                .phuongThuc(request.getPhuongThucThanhToan())
                .trangThai("Thành công")
                .build();
        thanhToanRepository.save(thanhToan);

        // Cap nhat trang thai DatVe -> 'Da thanh toan' (PROC_CAP_NHAT_THANH_TOAN)
        datVe.setTrangThai("Đã thanh toán");
        datVeRepository.save(datVe);

        // 7. Giai phong ghe giu tam (in-memory)
        seatHoldService.release(request.getMaLich(), maKh);

        log.info("Dat ve thanh cong: maDat={}, maKh={}, soGhe={}, tongTien={}",
                maDat, maKh, request.getMaGhes().size(), tongTienSauGiam);

        // Build response
        List<DatVeResponseDTO.GheInfoDTO> gheInfos = danhSachVe.stream()
                .map(ve -> DatVeResponseDTO.GheInfoDTO.builder()
                        .maVe(ve.getMaVe())
                        .hangGhe(ve.getGhe().getHangGhe())
                        .soGhe(ve.getGhe().getSoGhe())
                        .loaiGhe(ve.getGhe().getLoaiGhe())
                        .giaVe(ve.getGiaVe())
                        .build())
                .collect(Collectors.toList());

        return DatVeResponseDTO.builder()
                .maDat(maDat)
                .tenPhim(lichChieu.getPhim().getTenPhim())
                .tenPhong(lichChieu.getPhongChieu().getTenPhong())
                .tenRap(lichChieu.getPhongChieu().getRapChieu().getTenRap())
                .thoiGianChieu(lichChieu.getThoiGianBatDau())
                .gheDaDat(gheInfos)
                .tongTienGoc(tongTienGoc)
                .soTienGiam(soTienGiam)
                .tongTienSauGiam(tongTienSauGiam)
                .trangThai("Đã thanh toán")
                .maTt(maTt)
                .build();
    }

    /**
     * Huy dat ve neu chua thanh toan hoac chua den gio chieu.
     */
    @Transactional
    public void huyDatVe(String maDat, String maKh) {
        DatVe datVe = datVeRepository.findById(maDat)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt vé", "MA_DAT", maDat));

        // Kiem tra quyen huy (chi KH so huu moi huy duoc)
        if (!datVe.getKhachHang().getMaKh().equals(maKh)) {
            throw new BusinessException("Bạn không có quyền hủy đơn này");
        }

        if ("Đã thanh toán".equals(datVe.getTrangThai())) {
            throw new BusinessException("Đơn đã thanh toán không thể hủy qua hệ thống. Vui lòng liên hệ rạp.");
        }

        // Cap nhat trang thai tat ca ve -> 'Da huy'
        List<Ve> ves = veRepository.findByDatVeMaDat(maDat);
        for (Ve ve : ves) {
            ve.setTrangThai("Đã hủy");
            veRepository.save(ve);
        }

        datVe.setTrangThai("Đã hủy");
        datVeRepository.save(datVe);

        log.info("Huy dat ve thanh cong: maDat={}", maDat);
    }

    /**
     * Tinh gia ve theo loai ghe (VIP them 30%, Doi them 50%, Thuong giu nguyen).
     */
    private BigDecimal tinhGiaVeTheoLoaiGhe(BigDecimal giaGoc, String loaiGhe) {
        if ("VIP".equals(loaiGhe)) {
            return giaGoc.multiply(new BigDecimal("1.3")).setScale(0, RoundingMode.HALF_UP);
        } else if ("Đôi".equals(loaiGhe)) {
            return giaGoc.multiply(new BigDecimal("1.5")).setScale(0, RoundingMode.HALF_UP);
        }
        return giaGoc;
    }

    /**
     * Tinh so tien giam theo khuyen mai.
     * Logic: PERCENT -> giam theo %, AMOUNT -> giam so tien co dinh.
     */
    private BigDecimal tinhGiamGia(BigDecimal tongTien, KhuyenMai km) {
        if ("PERCENT".equals(km.getLoaiGiam())) {
            return tongTien.multiply(km.getGiaTri())
                    .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
        } else if ("AMOUNT".equals(km.getLoaiGiam())) {
            return km.getGiaTri().min(tongTien); // Khong giam qua tong tien
        }
        return BigDecimal.ZERO;
    }

    private String generateMaDat() {
        // Dung count + 1 nhung xu ly collision bang retry
        // De tranh truong hop count() bi sai khi co ban ghi bi xoa,
        // lay theo so lon hon gia tri lon nhat hien tai
        long count = datVeRepository.count() + 1;
        String maCandidate = String.format("DV%06d", count);
        // Neu da ton tai, tang len den khi tim duoc ma chua dung
        while (datVeRepository.existsById(maCandidate)) {
            count++;
            maCandidate = String.format("DV%06d", count);
        }
        return maCandidate;
    }

    private String generateMaVe() {
        long count = veRepository.count() + 1;
        String maCandidate = String.format("VE%06d", count);
        while (veRepository.existsById(maCandidate)) {
            count++;
            maCandidate = String.format("VE%06d", count);
        }
        return maCandidate;
    }

    private String generateMaTt() {
        long count = thanhToanRepository.count() + 1;
        String maCandidate = String.format("TT%06d", count);
        while (thanhToanRepository.existsById(maCandidate)) {
            count++;
            maCandidate = String.format("TT%06d", count);
        }
        return maCandidate;
    }
}
