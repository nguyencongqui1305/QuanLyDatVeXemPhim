package com.qlrapphim.service;

import com.qlrapphim.entity.KhuyenMai;
import com.qlrapphim.repository.KhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KhuyenMaiService {

    private final KhuyenMaiRepository khuyenMaiRepository;

    public List<KhuyenMai> findAll() {
        return khuyenMaiRepository.findAll();
    }

    public List<KhuyenMai> findHieuLuc() {
        return khuyenMaiRepository.findHieuLuc(LocalDate.now());
    }

    public Optional<KhuyenMai> findById(String maKm) {
        return khuyenMaiRepository.findById(maKm);
    }

    @Transactional
    public KhuyenMai save(KhuyenMai km) {
        if (km.getMaKm() == null || km.getMaKm().isBlank()) {
            long count = khuyenMaiRepository.count();
            km.setMaKm(String.format("KM%03d", count + 1));
        }
        return khuyenMaiRepository.save(km);
    }

    @Transactional
    public void delete(String maKm) {
        khuyenMaiRepository.deleteById(maKm);
    }
}
