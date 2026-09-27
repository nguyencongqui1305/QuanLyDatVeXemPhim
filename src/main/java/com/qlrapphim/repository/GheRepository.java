package com.qlrapphim.repository;

import com.qlrapphim.entity.Ghe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GheRepository extends JpaRepository<Ghe, String> {
    List<Ghe> findByPhongChieuMaPhongOrderByHangGheAscSoGheAsc(String maPhong);

    Optional<Ghe> findByPhongChieuMaPhongAndHangGheAndSoGhe(String maPhong, String hangGhe, Integer soGhe);

    /**
     * Lay danh sach ghe trong (chua bi dat hoac giu tam) cho mot suat chieu.
     * Day la implementation cua FUNC_GHE_TRONG tu tai lieu.
     */
    @Query("""
        SELECT g FROM Ghe g
        WHERE g.phongChieu.maPhong = :maPhong
        AND g.trangThai = 'Hoạt động'
        AND NOT EXISTS (
            SELECT v FROM Ve v
            WHERE v.ghe.maGhe = g.maGhe
            AND v.lichChieu.maLich = :maLich
            AND v.trangThai <> 'Đã hủy'
        )
        ORDER BY g.hangGhe ASC, g.soGhe ASC
        """)
    List<Ghe> findGheTrong(@Param("maPhong") String maPhong, @Param("maLich") String maLich);

    long countByPhongChieuMaPhong(String maPhong);
}
