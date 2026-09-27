package com.qlrapphim.service;

import com.qlrapphim.entity.*;
import com.qlrapphim.exception.*;
import com.qlrapphim.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GheService {

    private final GheRepository gheRepository;
    private final LichChieuRepository lichChieuRepository;
    private final KhachHangRepository khachHangRepository;
    private final VeRepository veRepository;
    private final SeatHoldService seatHoldService;

    public List<Ghe> findByPhong(String maPhong) {
        return gheRepository.findByPhongChieuMaPhongOrderByHangGheAscSoGheAsc(maPhong);
    }

    /**
     * Lay trang thai tung ghe cho mot suat chieu.
     * Dung de ve so do ghe tren giao dien.
     */
    public Map<String, String> getTrangThaiGheForLich(String maLich, String maKhHienTai) {
        LichChieu lichChieu = lichChieuRepository.findById(maLich)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch chiếu", "MA_LICH", maLich));

        List<Ghe> tatCaGhe = gheRepository.findByPhongChieuMaPhongOrderByHangGheAscSoGheAsc(
                lichChieu.getPhongChieu().getMaPhong());

        // Lay danh sach ghe da dat (TRANG_THAI != 'Đã hủy')
        List<Ve> veDaDat = veRepository.findByLichChieuMaLichAndTrangThaiNot(maLich, "Đã hủy");
        List<String> maGheDaDat = veDaDat.stream()
                .map(v -> v.getGhe().getMaGhe())
                .collect(Collectors.toList());

        // Dung placeholder neu chua dang nhap
        final String maKhCheck = (maKhHienTai != null && !maKhHienTai.isBlank())
                ? maKhHienTai : "__GUEST__";

        return tatCaGhe.stream().collect(Collectors.toMap(
                Ghe::getMaGhe,
                ghe -> {
                    if (!"Hoạt động".equals(ghe.getTrangThai())) return "BAO_TRI";
                    if (maGheDaDat.contains(ghe.getMaGhe())) return "DA_DAT";
                    if (seatHoldService.isHeldByOther(maLich, ghe.getMaGhe(), maKhCheck)) return "GIU_TAM";
                    return "TRONG";
                }
        ));
    }

    /**
     * Giu ghe tam thoi cho khach hang dang thanh toan.
     */
    @Transactional
    public void giuGheTam(String maLich, List<String> maGhes, String maKh) {
        // Kiem tra lich chieu va khach hang ton tai
        lichChieuRepository.findById(maLich)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch chiếu", "MA_LICH", maLich));
        khachHangRepository.findById(maKh)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "MA_KH", maKh));

        for (String maGhe : maGhes) {
            Ghe ghe = gheRepository.findById(maGhe)
                    .orElseThrow(() -> new ResourceNotFoundException("Ghế", "MA_GHE", maGhe));

            // Kiem tra ghe da bi dat chua
            Optional<Ve> veDaDat = veRepository.findVeDaDat(maLich, maGhe);
            if (veDaDat.isPresent()) {
                throw new GheDaDatException(
                        String.format("Ghế %s%d đã được đặt bởi người khác. Vui lòng chọn ghế khác.",
                                ghe.getHangGhe(), ghe.getSoGhe()));
            }

            // Kiem tra ghe co dang bi giu boi nguoi khac
            if (seatHoldService.isHeldByOther(maLich, maGhe, maKh)) {
                throw new GheDaDatException(
                        String.format("Ghế %s%d đang được người khác giữ tạm. Vui lòng chọn ghế khác.",
                                ghe.getHangGhe(), ghe.getSoGhe()));
            }

            // Giu ghe trong bo nho
            seatHoldService.hold(maLich, maGhe, maKh);
        }
        log.info("Giu tam {} ghe cho KH {} suất {}", maGhes.size(), maKh, maLich);
    }

    /**
     * Giai phong ghe giu tam khi dat ve thanh cong hoac khach huy.
     */
    public void giaiPhongGiuTam(String maLich, String maKh) {
        seatHoldService.release(maLich, maKh);
        log.info("Giai phong ghe giu tam cho KH {} suất {}", maKh, maLich);
    }

    @Transactional
    public Ghe save(Ghe ghe) {
        return gheRepository.save(ghe);
    }

    public long countByPhong(String maPhong) {
        return gheRepository.countByPhongChieuMaPhong(maPhong);
    }

    /**
     * Tự động tạo ghế cho phòng chiếu mới.
     * Layout: A/B = Thường (10 ghế), C/D = VIP (10 ghế), E = Đôi (8 ghế) = 48 ghế
     */
    @Transactional
    public void generateGheForPhong(PhongChieu phongChieu) {
        // Nếu phòng đã có ghế thì bỏ qua
        if (gheRepository.countByPhongChieuMaPhong(phongChieu.getMaPhong()) > 0) {
            log.info("Phòng {} đã có ghế, bỏ qua generate.", phongChieu.getMaPhong());
            return;
        }

        // Lấy số thứ tự ghế lớn nhất hiện tại để tạo mã không trùng
        long maxGheId = gheRepository.count();

        String[][] layout = {
            {"A", "Thường", "10"},
            {"B", "Thường", "10"},
            {"C", "VIP",    "10"},
            {"D", "VIP",    "10"},
            {"E", "Đôi",    "8"}
        };

        List<Ghe> ghes = new ArrayList<>();
        for (String[] row : layout) {
            String hang     = row[0];
            String loaiGhe  = row[1];
            int    soGheMax = Integer.parseInt(row[2]);
            for (int soGhe = 1; soGhe <= soGheMax; soGhe++) {
                maxGheId++;
                Ghe g = Ghe.builder()
                        .maGhe(String.format("G%03d", maxGheId))
                        .phongChieu(phongChieu)
                        .hangGhe(hang)
                        .soGhe(soGhe)
                        .loaiGhe(loaiGhe)
                        .trangThai("Hoạt động")
                        .build();
                ghes.add(g);
            }
        }
        gheRepository.saveAll(ghes);
        log.info("Đã tạo {} ghế cho phòng {}", ghes.size(), phongChieu.getMaPhong());
    }

    /**
     * Tim ghe theo ma ghe. Throw ResourceNotFoundException neu khong tim thay.
     */
    public Ghe findById(String maGhe) {
        return gheRepository.findById(maGhe)
                .orElseThrow(() -> new ResourceNotFoundException("Ghế", "MA_GHE", maGhe));
    }
}
