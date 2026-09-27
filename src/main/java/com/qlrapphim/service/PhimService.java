package com.qlrapphim.service;

import com.qlrapphim.entity.Phim;
import com.qlrapphim.entity.TheLoai;
import com.qlrapphim.exception.ResourceNotFoundException;
import com.qlrapphim.repository.PhimRepository;
import com.qlrapphim.repository.TheLoaiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhimService {

    private final PhimRepository phimRepository;
    private final TheLoaiRepository theLoaiRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Phim> findAll() {
        return phimRepository.findAll();
    }

    public List<Phim> findDangChieu() {
        return phimRepository.findByTrangThaiContainingIgnoreCaseOrderByNgayKhoiChieuDesc("ang");
    }

    public List<Phim> findSapChieu() {
        return phimRepository.findByTrangThaiContainingIgnoreCaseOrderByNgayKhoiChieuDesc("sap");
    }

    public Optional<Phim> findById(String maPhim) {
        return phimRepository.findById(maPhim);
    }

    public Phim getById(String maPhim) {
        return phimRepository.findById(maPhim)
                .orElseThrow(() -> new ResourceNotFoundException("Phim", "MA_PHIM", maPhim));
    }

    public Page<Phim> search(String tenPhim, String maTheLoai, String trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return phimRepository.searchPhim(
                (tenPhim != null && !tenPhim.isBlank()) ? tenPhim : null,
                (maTheLoai != null && !maTheLoai.isBlank()) ? maTheLoai : null,
                (trangThai != null && !trangThai.isBlank()) ? trangThai : null,
                pageable
        );
    }

    @Transactional
    public Phim save(Phim phim) {
        boolean isNew = (phim.getMaPhim() == null || phim.getMaPhim().isBlank());
        if (isNew) {
            phim.setMaPhim(generateMaPhim());
        }
        log.info("[PhimService.save] Đang lưu phim: maPhim={}, tenPhim={}, trangThai={}",
                phim.getMaPhim(), phim.getTenPhim(), phim.getTrangThai());

        // Re-load TheLoai trong cùng transaction để đảm bảo managed entity.
        // Nếu không làm bước này, TheLoai đã bị detached từ controller
        // → Hibernate merge qua connection pool khác → mất oracle.jdbc.defaultNChar → Mojibake.
        if (phim.getTheLoais() != null && !phim.getTheLoais().isEmpty()) {
            List<String> maTheLoais = phim.getTheLoais().stream()
                    .map(TheLoai::getMaTheLoai).toList();
            List<TheLoai> managedTheLoais = theLoaiRepository.findAllById(maTheLoais);
            log.info("[PhimService.save] The loai managed: {}", maTheLoais);
            phim.setTheLoais(managedTheLoais);
        } else {
            phim.setTheLoais(new ArrayList<>());
        }

        try {
            Phim saved = phimRepository.save(phim);
            // Flush ngay để phát hiện lỗi DB trong transaction này
            phimRepository.flush();
            log.info("[PhimService.save] Lưu thành công: maPhim={}", saved.getMaPhim());
            return saved;
        } catch (Exception e) {
            log.error("[PhimService.save] LỖI khi lưu phim maPhim={}: {}", phim.getMaPhim(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void delete(String maPhim) {
        log.info("[PhimService.delete] Đang xóa phim: maPhim={}", maPhim);
        Phim phim = phimRepository.findById(maPhim)
                .orElseThrow(() -> new ResourceNotFoundException("Phim", "MA_PHIM", maPhim));

        // Kiểm tra có lịch chiếu chưa - force load lazy collection
        int soLichChieu = phim.getLichChieus() != null ? phim.getLichChieus().size() : 0;
        if (soLichChieu > 0) {
            log.warn("[PhimService.delete] Không thể xóa phim {} - có {} lịch chiếu", maPhim, soLichChieu);
            throw new com.qlrapphim.exception.BusinessException(
                "Không thể xóa phim '" + phim.getTenPhim() + "' vì đã có " + soLichChieu + " lịch chiếu!");
        }

        // Xóa liên kết PHIM_THE_LOAI trước (tránh FK constraint)
        phim.setTheLoais(new ArrayList<>());
        phimRepository.save(phim);
        phimRepository.flush();

        // Xóa phim
        phimRepository.deleteById(maPhim);
        phimRepository.flush();
        log.info("[PhimService.delete] Xóa phim {} thành công", maPhim);
    }

    public List<TheLoai> findAllTheLoai() {
        return theLoaiRepository.findAllByOrderByTenTheLoaiAsc();
    }

    private String generateMaPhim() {
        String maxMa = phimRepository.findMaxMaPhim();
        if (maxMa == null) return "PH001";
        try {
            // Parse số từ "PH007" → 7, rồi +1
            int nextNum = Integer.parseInt(maxMa.replaceAll("[^0-9]", "")) + 1;
            return String.format("PH%03d", nextNum);
        } catch (NumberFormatException e) {
            return String.format("PH%03d", phimRepository.count() + 1);
        }
    }
}
