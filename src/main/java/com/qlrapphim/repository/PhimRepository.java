package com.qlrapphim.repository;

import com.qlrapphim.entity.Phim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhimRepository extends JpaRepository<Phim, String> {

    List<Phim> findByTrangThai(String trangThai);

    List<Phim> findByTrangThaiOrderByNgayKhoiChieuDesc(String trangThai);

    // Dung Contains de tranh loi encoding khi so sanh exact string
    List<Phim> findByTrangThaiContainingIgnoreCaseOrderByNgayKhoiChieuDesc(String keyword);

    @Query("""
        SELECT DISTINCT p FROM Phim p
        LEFT JOIN p.theLoais tl
        WHERE (:tenPhim IS NULL OR LOWER(p.tenPhim) LIKE LOWER(CONCAT('%', :tenPhim, '%')))
        AND (:maTheLoai IS NULL OR tl.maTheLoai = :maTheLoai)
        AND (:trangThai IS NULL OR p.trangThai = :trangThai)
        ORDER BY p.ngayKhoiChieu DESC
        """)
    Page<Phim> searchPhim(
            @Param("tenPhim") String tenPhim,
            @Param("maTheLoai") String maTheLoai,
            @Param("trangThai") String trangThai,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p FROM Phim p
        JOIN p.lichChieus lc
        JOIN lc.phongChieu pc
        WHERE pc.rapChieu.maRap = :maRap
        """)
    List<Phim> findPhimDangChieuByRap(@Param("maRap") String maRap);

    long count();

    @Query("SELECT MAX(p.maPhim) FROM Phim p")
    String findMaxMaPhim();
}
