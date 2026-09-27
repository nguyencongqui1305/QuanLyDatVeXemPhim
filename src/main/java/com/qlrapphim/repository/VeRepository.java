package com.qlrapphim.repository;

import com.qlrapphim.entity.Ve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VeRepository extends JpaRepository<Ve, String> {

    List<Ve> findByDatVeMaDat(String maDat);

    /**
     * Kiem tra ghe da bi dat cho suat chieu nay chua (logic chong dat trung ghe).
     * Day la implementation cua PROC_THEM_VE trong tai lieu.
     * Ghe duoc coi la "da bi dat" neu ton tai ve KHONG phai trang thai 'Da huy'.
     */
    @Query("""
        SELECT v FROM Ve v
        WHERE v.lichChieu.maLich = :maLich
        AND v.ghe.maGhe = :maGhe
        AND v.trangThai <> 'Đã hủy'
        """)
    Optional<Ve> findVeDaDat(@Param("maLich") String maLich, @Param("maGhe") String maGhe);

    List<Ve> findByLichChieuMaLichAndTrangThaiNot(String maLich, String trangThai);

    /**
     * Tong doanh thu theo suat chieu (FUNC_DOANH_THU_SUAT trong tai lieu).
     * Tinh tong gia ve cua cac ve khong phai da huy.
     */
    @Query("""
        SELECT COALESCE(SUM(v.giaVe), 0) FROM Ve v
        WHERE v.lichChieu.maLich = :maLich
        AND v.trangThai <> 'Đã hủy'
        """)
    BigDecimal tinhDoanhThuSuat(@Param("maLich") String maLich);

    long countByLichChieuMaLichAndTrangThaiNot(String maLich, String trangThai);
}
