package com.qlrapphim.repository;

import com.qlrapphim.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, String> {
    Optional<NhanVien> findByEmail(String email);
    boolean existsByEmail(String email);
    List<NhanVien> findByRapChieuMaRap(String maRap);
    List<NhanVien> findByChucVu(String chucVu);

    @Query("""
        SELECT nv FROM NhanVien nv
        WHERE (:tenNv IS NULL OR LOWER(nv.hoTen) LIKE LOWER(CONCAT('%', :tenNv, '%')))
        AND (:maRap IS NULL OR nv.rapChieu.maRap = :maRap)
        ORDER BY nv.hoTen ASC
        """)
    List<NhanVien> searchNhanVien(@Param("tenNv") String tenNv, @Param("maRap") String maRap);
}
