package com.qlrapphim.repository;

import com.qlrapphim.entity.RapChieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RapChieuRepository extends JpaRepository<RapChieu, String> {
    List<RapChieu> findAllByOrderByTenRapAsc();
}
