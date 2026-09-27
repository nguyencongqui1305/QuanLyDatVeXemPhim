package com.qlrapphim.repository;

import com.qlrapphim.entity.LichChieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LichChieuRepository extends JpaRepository<LichChieu, String> {

    /**
     * Load LichChieu với đầy đủ các quan hệ (JOIN FETCH) để tránh LazyInitializationException
     * và đảm bảo maPhong, tenRap, tenPhim luôn có giá trị.
     */
    @Query("""
        SELECT lc FROM LichChieu lc
        JOIN FETCH lc.phongChieu pc
        JOIN FETCH pc.rapChieu
        JOIN FETCH lc.phim
        WHERE lc.maLich = :maLich
        """)
    Optional<LichChieu> findByIdWithDetails(@Param("maLich") String maLich);

    // Tim theo exact match
    List<LichChieu> findByPhimMaPhimAndTrangThaiOrderByThoiGianBatDauAsc(String maPhim, String trangThai);

    // Tim voi keyword trong trang thai (tranh loi encoding)
    List<LichChieu> findByPhimMaPhimAndTrangThaiContainingIgnoreCaseOrderByThoiGianBatDauAsc(String maPhim, String keyword);

    @Query("""
        SELECT lc FROM LichChieu lc
        WHERE lc.phim.maPhim = :maPhim
        AND lc.phongChieu.rapChieu.maRap = :maRap
        AND lc.thoiGianBatDau >= :ngayBatDau
        AND lc.thoiGianBatDau < :ngayKetThuc
        ORDER BY lc.thoiGianBatDau ASC
        """)
    List<LichChieu> findLichChieuByPhimRapNgay(
            @Param("maPhim") String maPhim,
            @Param("maRap") String maRap,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc
    );

    @Query("""
        SELECT lc FROM LichChieu lc
        WHERE lc.phongChieu.maPhong = :maPhong
        AND lc.thoiGianBatDau < :ketThuc
        AND lc.thoiGianKetThuc > :batDau
        AND lc.maLich <> :maLichLoaiTru
        """)
    List<LichChieu> findConflictingSchedule(
            @Param("maPhong") String maPhong,
            @Param("batDau") LocalDateTime batDau,
            @Param("ketThuc") LocalDateTime ketThuc,
            @Param("maLichLoaiTru") String maLichLoaiTru
    );

    List<LichChieu> findByPhongChieuMaPhongAndTrangThaiOrderByThoiGianBatDauAsc(String maPhong, String trangThai);

    @Query("""
        SELECT lc FROM LichChieu lc
        WHERE lc.thoiGianBatDau > CURRENT_TIMESTAMP
        ORDER BY lc.thoiGianBatDau ASC
        """)
    List<LichChieu> findUpcomingSchedules();

    long count();
}
