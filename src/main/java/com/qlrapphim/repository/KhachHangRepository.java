package com.qlrapphim.repository;

import com.qlrapphim.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findBySdt(String sdt);
    boolean existsByEmail(String email);
    boolean existsBySdt(String sdt);
    long count();
}
