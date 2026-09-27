package com.qlrapphim.controller.admin;

import com.qlrapphim.entity.*;
import com.qlrapphim.exception.ResourceNotFoundException;
import com.qlrapphim.repository.PhongChieuRepository;
import com.qlrapphim.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/lich-chieu")
@PreAuthorize("hasAnyRole('NHAN_VIEN', 'QUAN_LY')")
@RequiredArgsConstructor
public class AdminLichChieuController {

    private final LichChieuService lichChieuService;
    private final PhimService phimService;
    private final RapChieuService rapChieuService;
    private final PhongChieuRepository phongChieuRepository;

    @GetMapping
    public String danhSach(Model model) {
        List<LichChieu> danhSach = lichChieuService.findAll();
        model.addAttribute("danhSachLichChieu", danhSach);
        model.addAttribute("pageTitle", "Quản lý lịch chiếu - UTC Cinema Admin");
        return "admin/quan-ly-lich-chieu";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("danhSachPhim", phimService.findAll());
        model.addAttribute("danhSachRap", rapChieuService.findAll());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm lịch chiếu - UTC Cinema Admin");
        return "admin/lich-chieu-form";
    }

    @PostMapping("/them")
    public String themLich(
            @RequestParam String maPhim,
            @RequestParam String maPhong,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime thoiGianBatDau,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime thoiGianKetThuc,
            @RequestParam BigDecimal giaVe,
            @RequestParam(defaultValue = "Mở bán") String trangThai,
            RedirectAttributes redirectAttributes) {

        try {
            Phim phim = phimService.getById(maPhim);
            PhongChieu phong = phongChieuRepository.findById(maPhong)
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng chiếu", "MA_PHONG", maPhong));

            LichChieu lc = LichChieu.builder()
                    .phim(phim)
                    .phongChieu(phong)
                    .thoiGianBatDau(thoiGianBatDau)
                    .thoiGianKetThuc(thoiGianKetThuc)
                    .giaVe(giaVe)
                    .trangThai(trangThai)
                    .build();
            lichChieuService.save(lc);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm lịch chiếu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/lich-chieu";
    }

    @PostMapping("/xoa/{maLich}")
    public String xoaLich(@PathVariable String maLich, RedirectAttributes redirectAttributes) {
        try {
            lichChieuService.delete(maLich);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa lịch chiếu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/lich-chieu";
    }

    @PostMapping("/trang-thai/{maLich}")
    public String capNhatTrangThai(
            @PathVariable String maLich,
            @RequestParam String trangThai,
            RedirectAttributes redirectAttributes) {
        try {
            lichChieuService.updateTrangThai(maLich, trangThai);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/lich-chieu";
    }
}
