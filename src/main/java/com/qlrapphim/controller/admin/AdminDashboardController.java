package com.qlrapphim.controller.admin;

import com.qlrapphim.service.BaoCaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('NHAN_VIEN', 'QUAN_LY')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final BaoCaoService baoCaoService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        Map<String, Object> stats = baoCaoService.getDashboardStats();
        model.addAttribute("stats", stats);

        // Du lieu cho bieu do doanh thu theo phim
        List<Object[]> doanhThuPhim = baoCaoService.getDoanhThuTheoPhim();
        model.addAttribute("doanhThuPhim", doanhThuPhim);

        // Du lieu cho bieu do phuong thuc thanh toan
        List<Object[]> doanhThuPhuongThuc = baoCaoService.getDoanhThuTheoPhuongThuc();
        model.addAttribute("doanhThuPhuongThuc", doanhThuPhuongThuc);

        model.addAttribute("pageTitle", "Dashboard - UTC Cinema Admin");
        return "admin/dashboard";
    }
}
