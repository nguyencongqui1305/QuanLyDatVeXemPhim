package com.qlrapphim.service;

import com.qlrapphim.entity.LichChieu;
import com.qlrapphim.entity.PhongChieu;
import com.qlrapphim.entity.Phim;
import com.qlrapphim.exception.BusinessException;
import com.qlrapphim.exception.ResourceNotFoundException;
import com.qlrapphim.repository.LichChieuRepository;
import com.qlrapphim.repository.PhimRepository;
import com.qlrapphim.repository.PhongChieuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LichChieuService {

    private final LichChieuRepository lichChieuRepository;
    private final PhimRepository phimRepository;
    private final PhongChieuRepository phongChieuRepository;

    public List<LichChieu> findAll() {
        return lichChieuRepository.findAll();
    }

    public Optional<LichChieu> findById(String maLich) {
        return lichChieuRepository.findByIdWithDetails(maLich);
    }

    public LichChieu getById(String maLich) {
        return lichChieuRepository.findByIdWithDetails(maLich)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch chiếu", "MA_LICH", maLich));
    }

    public List<LichChieu> findByPhimAndRapAndNgay(String maPhim, String maRap, LocalDateTime ngay) {
        LocalDateTime startOfDay = ngay.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return lichChieuRepository.findLichChieuByPhimRapNgay(maPhim, maRap, startOfDay, endOfDay);
    }

    public List<LichChieu> findByPhim(String maPhim) {
        // Dung keyword 'ban' de bat ca 'Mo ban' va 'Mở bán' (trang thai co the bi encoding khac)
        return lichChieuRepository.findByPhimMaPhimAndTrangThaiContainingIgnoreCaseOrderByThoiGianBatDauAsc(maPhim, "ban");
    }

    @Transactional
    public LichChieu save(LichChieu lichChieu) {
        // Validate: thoi gian ket thuc phai lon hon bat dau
        if (!lichChieu.getThoiGianKetThuc().isAfter(lichChieu.getThoiGianBatDau())) {
            throw new BusinessException("Thời gian kết thúc phải lớn hơn thời gian bắt đầu");
        }

        // Validate: gia ve phai >= 0
        if (lichChieu.getGiaVe().signum() < 0) {
            throw new BusinessException("Giá vé phải lớn hơn hoặc bằng 0");
        }

        // Kiem tra trung gio trong cung phong chieu
        String maLichLoaiTru = lichChieu.getMaLich() != null ? lichChieu.getMaLich() : "__NONE__";
        List<LichChieu> conflicts = lichChieuRepository.findConflictingSchedule(
                lichChieu.getPhongChieu().getMaPhong(),
                lichChieu.getThoiGianBatDau(),
                lichChieu.getThoiGianKetThuc(),
                maLichLoaiTru);

        if (!conflicts.isEmpty()) {
            throw new BusinessException(
                    "Phòng chiếu đã có suất chiếu khác trong khoảng thời gian này");
        }

        if (lichChieu.getMaLich() == null || lichChieu.getMaLich().isBlank()) {
            lichChieu.setMaLich(generateMaLich());
        }

        return lichChieuRepository.save(lichChieu);
    }

    @Transactional
    public void delete(String maLich) {
        lichChieuRepository.deleteById(maLich);
    }

    @Transactional
    public void updateTrangThai(String maLich, String trangThai) {
        LichChieu lc = getById(maLich);
        lc.setTrangThai(trangThai);
        lichChieuRepository.save(lc);
    }

    private String generateMaLich() {
        long count = lichChieuRepository.count();
        return String.format("LC%05d", count + 1);
    }
}
