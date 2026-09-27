package com.qlrapphim.service;

import com.qlrapphim.entity.RapChieu;
import com.qlrapphim.entity.PhongChieu;
import com.qlrapphim.exception.ResourceNotFoundException;
import com.qlrapphim.repository.RapChieuRepository;
import com.qlrapphim.repository.PhongChieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RapChieuService {

    private final RapChieuRepository rapChieuRepository;
    private final PhongChieuRepository phongChieuRepository;

    public List<RapChieu> findAll() {
        return rapChieuRepository.findAllByOrderByTenRapAsc();
    }

    public Optional<RapChieu> findById(String maRap) {
        return rapChieuRepository.findById(maRap);
    }

    public RapChieu getById(String maRap) {
        return rapChieuRepository.findById(maRap)
                .orElseThrow(() -> new ResourceNotFoundException("Rạp chiếu", "MA_RAP", maRap));
    }

    @Transactional
    public RapChieu save(RapChieu rapChieu) {
        return rapChieuRepository.save(rapChieu);
    }

    @Transactional
    public void delete(String maRap) {
        rapChieuRepository.deleteById(maRap);
    }

    public List<PhongChieu> findPhongByRap(String maRap) {
        return phongChieuRepository.findByRapChieuMaRap(maRap);
    }

    @Transactional
    public PhongChieu savePhong(PhongChieu phong) {
        if (phong.getMaPhong() == null || phong.getMaPhong().isBlank()) {
            long count = phongChieuRepository.count();
            phong.setMaPhong(String.format("P%04d", count + 1));
        }
        return phongChieuRepository.save(phong);
    }
}
