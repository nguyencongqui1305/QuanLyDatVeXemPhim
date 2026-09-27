package com.qlrapphim.controller.client;

import com.qlrapphim.entity.Phim;
import com.qlrapphim.entity.RapChieu;
import com.qlrapphim.entity.TheLoai;
import com.qlrapphim.service.PhimService;
import com.qlrapphim.service.RapChieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PhimService phimService;
    private final RapChieuService rapChieuService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Phim> dangChieu = phimService.findDangChieu();
        List<Phim> sapChieu = phimService.findSapChieu();
        List<RapChieu> danhSachRap = rapChieuService.findAll();
        List<TheLoai> danhSachTheLoai = phimService.findAllTheLoai();

        model.addAttribute("phimDangChieu", dangChieu);
        model.addAttribute("phimSapChieu", sapChieu);
        model.addAttribute("danhSachRap", danhSachRap);
        model.addAttribute("danhSachTheLoai", danhSachTheLoai);
        model.addAttribute("pageTitle", "UTC Cinema - Đặt Vé Xem Phim Online");
        return "client/home";
    }

    @GetMapping("/tim-kiem")
    public String timKiem(
            @RequestParam(required = false) String tenPhim,
            @RequestParam(required = false) String maTheLoai,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Phim> ketQua = phimService.search(tenPhim, maTheLoai, trangThai, page, 12);
        List<TheLoai> danhSachTheLoai = phimService.findAllTheLoai();

        model.addAttribute("phims", ketQua);
        model.addAttribute("danhSachTheLoai", danhSachTheLoai);
        model.addAttribute("tenPhimSearch", tenPhim);
        model.addAttribute("maTheLoaiSearch", maTheLoai);
        model.addAttribute("trangThaiSearch", trangThai);
        model.addAttribute("pageTitle", "Tìm kiếm phim - UTC Cinema");
        return "client/tim-kiem";
    }

    @GetMapping("/403")
    public String forbidden(Model model) {
        model.addAttribute("pageTitle", "Truy cập bị từ chối - UTC Cinema");
        return "error/403";
    }
}
