package com.qlrapphim.controller.client;

import com.qlrapphim.dto.RegisterDTO;
import com.qlrapphim.entity.KhachHang;
import com.qlrapphim.exception.BusinessException;
import com.qlrapphim.security.UserPrincipal;
import com.qlrapphim.service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final KhachHangService khachHangService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Đăng nhập - UTC Cinema");
        return "client/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("pageTitle", "Đăng ký tài khoản - UTC Cinema");
        return "client/register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Đăng ký tài khoản - UTC Cinema");
            return "client/register";
        }

        try {
            khachHangService.dangKy(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Đăng ký tài khoản - UTC Cinema");
            return "client/register";
        }
    }

    @GetMapping("/tai-khoan")
    public String taiKhoan(@AuthenticationPrincipal UserPrincipal user, Model model) {
        if (user != null && "KHACH_HANG".equals(user.getUserType())) {
            KhachHang kh = khachHangService.findById(user.getUserId()).orElse(null);
            model.addAttribute("khachHang", kh);
        }
        model.addAttribute("pageTitle", "Tài khoản của tôi - UTC Cinema");
        return "client/tai-khoan";
    }

    @PostMapping("/tai-khoan/cap-nhat")
    public String capNhatTaiKhoan(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam String hoTen,
            @RequestParam String sdt,
            RedirectAttributes redirectAttributes) {
        try {
            KhachHang kh = khachHangService.findById(user.getUserId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản"));
            kh.setHoTen(hoTen);
            kh.setSdt(sdt);
            khachHangService.save(kh);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/tai-khoan";
    }
}
