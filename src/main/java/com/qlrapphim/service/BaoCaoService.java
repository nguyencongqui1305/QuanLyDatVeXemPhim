package com.qlrapphim.service;

import com.qlrapphim.repository.DatVeRepository;
import com.qlrapphim.repository.KhachHangRepository;
import com.qlrapphim.repository.LichChieuRepository;
import com.qlrapphim.repository.PhimRepository;
import com.qlrapphim.repository.VeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BaoCaoService {

    private final DatVeRepository datVeRepository;
    private final VeRepository veRepository;
    private final LichChieuRepository lichChieuRepository;
    private final PhimRepository phimRepository;
    private final KhachHangRepository khachHangRepository;

    /**
     * Thong ke dashboard tong quan
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tongVeBan", datVeRepository.countTotalVeBan());
        stats.put("tongDoanhThu", datVeRepository.tinhTongDoanhThu());
        stats.put("soSuatMoBan", lichChieuRepository.count());
        stats.put("daDatVe", datVeRepository.countByTrangThai("Đã thanh toán"));
        stats.put("choThanhToan", datVeRepository.countByTrangThai("Chờ thanh toán"));
        stats.put("tongPhim", phimRepository.count());
        stats.put("tongKhachHang", khachHangRepository.count());
        return stats;
    }

    /**
     * Doanh thu theo phim (cho bieu do bar chart)
     */
    public List<Object[]> getDoanhThuTheoPhim() {
        return datVeRepository.doanhThuTheoPhim();
    }

    /**
     * Doanh thu theo rap
     */
    public List<Object[]> getDoanhThuTheoRap() {
        return datVeRepository.doanhThuTheoRap();
    }

    /**
     * Doanh thu theo phuong thuc thanh toan (cho bieu do pie chart)
     */
    public List<Object[]> getDoanhThuTheoPhuongThuc() {
        return datVeRepository.doanhThuTheoPhuongThuc();
    }

    /**
     * Doanh thu theo suat chieu (FUNC_DOANH_THU_SUAT)
     */
    public BigDecimal getDoanhThuSuat(String maLich) {
        return veRepository.tinhDoanhThuSuat(maLich);
    }
}
