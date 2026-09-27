package com.qlrapphim.controller.admin;

import com.qlrapphim.entity.DatVe;
import com.qlrapphim.repository.DatVeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller de quan ly tat ca don dat ve.
 * - Xem danh sach, loc theo trang thai / tu khoa
 * - Cap nhat trang thai don hang
 */
@Controller
@RequestMapping("/admin/dat-ve")
@PreAuthorize("hasAnyRole('NHAN_VIEN', 'QUAN_LY')")
@RequiredArgsConstructor
public class AdminDatVeController {

    private final DatVeRepository datVeRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String danhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            Model model) {

        // Load tat ca dat ve, sap xep moi nhat len tren
        List<DatVe> danhSach = datVeRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    if (a.getNgayDat() == null && b.getNgayDat() == null) return 0;
                    if (a.getNgayDat() == null) return 1;
                    if (b.getNgayDat() == null) return -1;
                    return b.getNgayDat().compareTo(a.getNgayDat());
                })
                .collect(Collectors.toList());

        // Filter theo trang thai
        if (trangThai != null && !trangThai.isBlank()) {
            danhSach = danhSach.stream()
                    .filter(dv -> trangThai.equalsIgnoreCase(dv.getTrangThai()))
                    .collect(Collectors.toList());
        }

        // Filter theo keyword (ma dat hoac ten khach hang)
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase().trim();
            danhSach = danhSach.stream()
                    .filter(dv ->
                            (dv.getMaDat() != null && dv.getMaDat().toLowerCase().contains(kw)) ||
                            (dv.getKhachHang() != null && dv.getKhachHang().getHoTen() != null
                             && dv.getKhachHang().getHoTen().toLowerCase().contains(kw)) ||
                            (dv.getKhachHang() != null && dv.getKhachHang().getEmail() != null
                             && dv.getKhachHang().getEmail().toLowerCase().contains(kw))
                    )
                    .collect(Collectors.toList());
        }

        // Stats
        long soThanhToan = datVeRepository.countByTrangThai("Đã thanh toán");
        long soChoPhanHoi = datVeRepository.countByTrangThai("Chờ thanh toán");
        long soHuy = datVeRepository.countByTrangThai("Đã hủy");
        BigDecimal tongDoanhThu = datVeRepository.tinhTongDoanhThu();

        model.addAttribute("danhSachDatVe", danhSach);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("soThanhToan", soThanhToan);
        model.addAttribute("soChoPhanHoi", soChoPhanHoi);
        model.addAttribute("soHuy", soHuy);
        model.addAttribute("tongDoanhThu", tongDoanhThu);
        model.addAttribute("pageTitle", "Quản lý đặt vé - UTC Cinema Admin");
        return "admin/quan-ly-dat-ve";
    }

    @PostMapping("/trang-thai/{maDat}")
    public String capNhatTrangThai(
            @PathVariable String maDat,
            @RequestParam String trangThai,
            RedirectAttributes ra) {
        try {
            DatVe datVe = datVeRepository.findById(maDat)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé: " + maDat));
            datVe.setTrangThai(trangThai);
            datVeRepository.save(datVe);
            ra.addFlashAttribute("successMessage",
                    "Đã cập nhật trạng thái đơn " + maDat + " → " + trangThai);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/dat-ve";
    }
}
