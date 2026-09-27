package com.qlrapphim.controller.admin;

import com.qlrapphim.service.BaoCaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/bao-cao")
@PreAuthorize("hasRole('QUAN_LY')")
@RequiredArgsConstructor
public class AdminBaoCaoController {

    private final BaoCaoService baoCaoService;

    @GetMapping
    public String baoCao(Model model) {
        List<Object[]> doanhThuPhim = baoCaoService.getDoanhThuTheoPhim();
        List<Object[]> doanhThuRap = baoCaoService.getDoanhThuTheoRap();
        List<Object[]> doanhThuPhuongThuc = baoCaoService.getDoanhThuTheoPhuongThuc();
        Map<String, Object> stats = baoCaoService.getDashboardStats();

        model.addAttribute("doanhThuPhim", doanhThuPhim);
        model.addAttribute("doanhThuRap", doanhThuRap);
        model.addAttribute("doanhThuPhuongThuc", doanhThuPhuongThuc);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Báo cáo thống kê - UTC Cinema Admin");
        return "admin/bao-cao";
    }
}
