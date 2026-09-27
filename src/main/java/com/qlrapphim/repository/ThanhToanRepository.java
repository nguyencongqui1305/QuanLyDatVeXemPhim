package com.qlrapphim.repository;

import com.qlrapphim.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {
    Optional<ThanhToan> findByDatVeMaDat(String maDat);
    List<ThanhToan> findAllByOrderByNgayTtDesc();
    List<ThanhToan> findByTrangThaiOrderByNgayTtDesc(String trangThai);
}
