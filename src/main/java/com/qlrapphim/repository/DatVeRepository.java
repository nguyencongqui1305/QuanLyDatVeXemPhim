package com.qlrapphim.repository;

import com.qlrapphim.entity.DatVe;
import com.qlrapphim.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DatVeRepository extends JpaRepository<DatVe, String> {

    List<DatVe> findByKhachHangOrderByNgayDatDesc(KhachHang khachHang);

    List<DatVe> findByKhachHangMaKhOrderByNgayDatDesc(String maKh);

    List<DatVe> findByTrangThaiOrderByNgayDatDesc(String trangThai);

    /**
     * Dem tong so ve da ban (khong bao gom ve da huy)
     */
    @Query("""
        SELECT COUNT(v) FROM Ve v
        WHERE v.trangThai <> 'Đã hủy'
        """)
    long countTotalVeBan();

    /**
     * Tinh tong doanh thu (tu cac don da thanh toan)
     */
    @Query("""
        SELECT COALESCE(SUM(dv.tongTien), 0) FROM DatVe dv
        WHERE dv.trangThai = 'Đã thanh toán'
        """)
    BigDecimal tinhTongDoanhThu();

    /**
     * Dem so don theo trang thai
     */
    long countByTrangThai(String trangThai);

    /**
     * Doanh thu theo phim
     */
    @Query("""
        SELECT lc.phim.tenPhim, COALESCE(SUM(v.giaVe), 0)
        FROM Ve v
        JOIN v.lichChieu lc
        WHERE v.trangThai <> 'Đã hủy'
        GROUP BY lc.phim.maPhim, lc.phim.tenPhim
        ORDER BY SUM(v.giaVe) DESC
        """)
    List<Object[]> doanhThuTheoPhim();

    /**
     * Doanh thu theo rap
     */
    @Query("""
        SELECT rc.tenRap, COALESCE(SUM(v.giaVe), 0)
        FROM Ve v
        JOIN v.lichChieu lc
        JOIN lc.phongChieu pc
        JOIN pc.rapChieu rc
        WHERE v.trangThai <> 'Đã hủy'
        GROUP BY rc.maRap, rc.tenRap
        ORDER BY SUM(v.giaVe) DESC
        """)
    List<Object[]> doanhThuTheoRap();

    /**
     * Doanh thu theo phuong thuc thanh toan
     */
    @Query("""
        SELECT tt.phuongThuc, COALESCE(SUM(tt.soTien), 0)
        FROM ThanhToan tt
        WHERE tt.trangThai = 'Thành công'
        GROUP BY tt.phuongThuc
        """)
    List<Object[]> doanhThuTheoPhuongThuc();

    long count();
}
