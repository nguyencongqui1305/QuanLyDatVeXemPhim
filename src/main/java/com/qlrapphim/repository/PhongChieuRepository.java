package com.qlrapphim.repository;

import com.qlrapphim.entity.PhongChieu;
import com.qlrapphim.entity.RapChieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhongChieuRepository extends JpaRepository<PhongChieu, String> {
    List<PhongChieu> findByRapChieu(RapChieu rapChieu);
    List<PhongChieu> findByRapChieuMaRap(String maRap);
}
