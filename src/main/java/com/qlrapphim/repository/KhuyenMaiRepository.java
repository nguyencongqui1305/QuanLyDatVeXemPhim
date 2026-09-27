package com.qlrapphim.repository;

import com.qlrapphim.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, String> {

    /**
     * Lay cac khuyen mai con hieu luc theo ngay hien tai
     */
    @Query("""
        SELECT km FROM KhuyenMai km
        WHERE (km.ngayBatDau IS NULL OR km.ngayBatDau <= :ngayHienTai)
        AND (km.ngayKetThuc IS NULL OR km.ngayKetThuc >= :ngayHienTai)
        ORDER BY km.tenKm ASC
        """)
    List<KhuyenMai> findHieuLuc(LocalDate ngayHienTai);
}
