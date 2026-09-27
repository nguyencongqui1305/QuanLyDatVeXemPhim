package com.qlrapphim.controller.admin;

import com.qlrapphim.entity.*;
import com.qlrapphim.exception.BusinessException;
import com.qlrapphim.repository.DatVeRepository;
import com.qlrapphim.repository.KhachHangRepository;
import com.qlrapphim.repository.NhanVienRepository;
import com.qlrapphim.repository.ThanhToanRepository;
import com.qlrapphim.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('NHAN_VIEN', 'QUAN_LY')")
@RequiredArgsConstructor
public class AdminQuanLyController {

    private final KhachHangService khachHangService;
    private final NhanVienRepository nhanVienRepository;
    private final RapChieuService rapChieuService;
    private final GheService gheService;
    private final KhuyenMaiService khuyenMaiService;
    private final DatVeRepository datVeRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final PasswordEncoder passwordEncoder;

    // ===================================================================
    // QUAN LY KHACH HANG
    // ===================================================================

    @GetMapping("/khach-hang")
    public String danhSachKhachHang(Model model) {
        model.addAttribute("danhSachKH", khachHangService.findAll());
        model.addAttribute("pageTitle", "Quản lý khách hàng - UTC Cinema Admin");
        return "admin/quan-ly-khach-hang";
    }

    @PostMapping("/khach-hang/xoa/{maKh}")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String xoaKhachHang(@PathVariable String maKh, RedirectAttributes ra) {
        try {
            khachHangService.delete(maKh);
            ra.addFlashAttribute("successMessage", "Xóa khách hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/khach-hang";
    }

    // ===================================================================
    // QUAN LY NHAN VIEN
    // ===================================================================

    @GetMapping("/nhan-vien")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String danhSachNhanVien(Model model) {
        model.addAttribute("danhSachNV", nhanVienRepository.findAll());
        model.addAttribute("danhSachRap", rapChieuService.findAll());
        model.addAttribute("pageTitle", "Quản lý nhân viên - UTC Cinema Admin");
        return "admin/quan-ly-nhan-vien";
    }

    @PostMapping("/nhan-vien/them")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String themNhanVien(
            @RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String sdt,
            @RequestParam String chucVu,
            @RequestParam String maRap,
            @RequestParam String matKhau,
            RedirectAttributes ra) {
        try {
            if (hoTen == null || hoTen.isBlank()) throw new BusinessException("Họ tên không được để trống");
            if (email == null || email.isBlank()) throw new BusinessException("Email không được để trống");
            if (maRap == null || maRap.isBlank()) throw new BusinessException("Vui lòng chọn rạp");
            if (matKhau == null || matKhau.length() < 6) throw new BusinessException("Mật khẩu phải tối thiểu 6 ký tự");

            // Generate mã nhân viên
            long count = nhanVienRepository.count() + 1;
            String maNv = String.format("NV%03d", count);
            while (nhanVienRepository.existsById(maNv)) {
                count++;
                maNv = String.format("NV%03d", count);
            }

            RapChieu rap = rapChieuService.getById(maRap);
            NhanVien nv = NhanVien.builder()
                    .maNv(maNv)
                    .hoTen(hoTen)
                    .email(email)
                    .sdt(sdt)
                    .chucVu(chucVu)
                    .rapChieu(rap)
                    .matKhau(passwordEncoder.encode(matKhau))
                    .build();
            nhanVienRepository.save(nv);
            ra.addFlashAttribute("successMessage", "Thêm nhân viên thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/nhan-vien";
    }

    @PostMapping("/nhan-vien/xoa/{maNv}")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String xoaNhanVien(@PathVariable String maNv, RedirectAttributes ra) {
        try {
            nhanVienRepository.deleteById(maNv);
            ra.addFlashAttribute("successMessage", "Xóa nhân viên thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/nhan-vien";
    }

    @PostMapping("/nhan-vien/sua")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String suaNhanVien(
            @RequestParam String maNv,
            @RequestParam String hoTen,
            @RequestParam String email,
            @RequestParam String sdt,
            @RequestParam String chucVu,
            @RequestParam String maRap,
            @RequestParam(required = false) String matKhauMoi,
            RedirectAttributes ra) {
        try {
            NhanVien nv = nhanVienRepository.findById(maNv)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));
            if (hoTen == null || hoTen.isBlank()) throw new BusinessException("Họ tên không được để trống");
            if (email == null || email.isBlank()) throw new BusinessException("Email không được để trống");

            nv.setHoTen(hoTen.trim());
            nv.setEmail(email.trim());
            nv.setSdt(sdt);
            nv.setChucVu(chucVu);
            nv.setRapChieu(rapChieuService.getById(maRap));
            if (matKhauMoi != null && !matKhauMoi.isBlank()) {
                if (matKhauMoi.length() < 6) throw new BusinessException("Mật khẩu mới phải tối thiểu 6 ký tự");
                nv.setMatKhau(passwordEncoder.encode(matKhauMoi));
            }
            nhanVienRepository.save(nv);
            ra.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/nhan-vien";
    }

    @PostMapping("/nhan-vien/khoa/{maNv}")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String khoaMoTaiKhoanNhanVien(@PathVariable String maNv, RedirectAttributes ra) {
        try {
            NhanVien nv = nhanVienRepository.findById(maNv)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));
            boolean dangKhoa = "Đã khóa".equals(nv.getTrangThai());
            nv.setTrangThai(dangKhoa ? "Hoạt động" : "Đã khóa");
            nhanVienRepository.save(nv);
            ra.addFlashAttribute("successMessage", dangKhoa
                    ? "Đã mở khóa tài khoản!" : "Đã khóa tài khoản!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/nhan-vien";
    }

    // ===================================================================
    // QUAN LY RAP / PHONG CHIEU
    // ===================================================================

    @GetMapping("/rap-chieu")
    public String danhSachRap(Model model) {
        model.addAttribute("danhSachRap", rapChieuService.findAll());
        model.addAttribute("pageTitle", "Quản lý rạp & phòng chiếu - UTC Cinema Admin");
        return "admin/quan-ly-rap";
    }

    @GetMapping("/rap-chieu/{maRap}/phong")
    @ResponseBody
    public List<PhongChieu> getPhongByRap(@PathVariable String maRap) {
        return rapChieuService.findPhongByRap(maRap);
    }

    @PostMapping("/rap-chieu/them")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String themRap(
            @RequestParam String tenRap,
            @RequestParam String diaChi,
            @RequestParam(required = false) String sdt,
            RedirectAttributes ra) {
        try {
            if (tenRap == null || tenRap.isBlank()) throw new BusinessException("Tên rạp không được để trống");
            if (diaChi == null || diaChi.isBlank()) throw new BusinessException("Địa chỉ không được để trống");

            // Chuyển chuỗi rỗng thành null để tránh vi phạm UNIQUE constraint trên cột SDT
            String sdtValue = (sdt != null && !sdt.isBlank()) ? sdt.trim() : null;

            // Tạo mã rạp không trùng
            long count = rapChieuService.findAll().size() + 1;
            String maRap = String.format("RAP%02d", count);
            // Đảm bảo mã không bị trùng
            while (rapChieuService.findById(maRap).isPresent()) {
                count++;
                maRap = String.format("RAP%02d", count);
            }

            RapChieu rap = RapChieu.builder()
                    .maRap(maRap)
                    .tenRap(tenRap.trim())
                    .diaChi(diaChi.trim())
                    .sdt(sdtValue)
                    .build();
            rapChieuService.save(rap);
            ra.addFlashAttribute("successMessage", "Thêm rạp thành công! Mã: " + maRap);
        } catch (Exception e) {
            // Log lỗi chi tiết để dễ debug
            ra.addFlashAttribute("errorMessage", "Lỗi khi thêm rạp: " + e.getMessage()
                    + (e.getCause() != null ? " | Nguyên nhân: " + e.getCause().getMessage() : ""));
        }
        return "redirect:/admin/rap-chieu";
    }

    @PostMapping("/rap-chieu/xoa/{maRap}")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String xoaRap(@PathVariable String maRap, RedirectAttributes ra) {
        try {
            rapChieuService.delete(maRap);
            ra.addFlashAttribute("successMessage", "Xóa rạp thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi xóa rạp (có thể còn dữ liệu liên quan): " + e.getMessage());
        }
        return "redirect:/admin/rap-chieu";
    }

    @PostMapping("/phong-chieu/them")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String themPhong(
            @RequestParam String maRap,
            @RequestParam String tenPhong,
            @RequestParam String loaiPhong,
            @RequestParam Integer sucChua,
            RedirectAttributes ra) {
        try {
            if (tenPhong == null || tenPhong.isBlank()) throw new BusinessException("Tên phòng không được để trống");
            if (sucChua == null || sucChua < 1) throw new BusinessException("Sức chứa phải lớn hơn 0");

            RapChieu rap = rapChieuService.getById(maRap);
            PhongChieu phong = PhongChieu.builder()
                    .tenPhong(tenPhong)
                    .loaiPhong(loaiPhong)
                    .sucChua(sucChua)
                    .rapChieu(rap)
                    .build();
            PhongChieu savedPhong = rapChieuService.savePhong(phong);

            // Tự động tạo 48 ghế cho phòng mới (A/B=Thường, C/D=VIP, E=Đôi)
            gheService.generateGheForPhong(savedPhong);

            ra.addFlashAttribute("successMessage",
                "Thêm phòng chiếu thành công! Đã tạo tự động 48 ghế.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/rap-chieu";
    }

    // ===================================================================
    // QUAN LY KHUYEN MAI
    // ===================================================================

    @GetMapping("/khuyen-mai")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String danhSachKhuyenMai(Model model) {
        model.addAttribute("danhSachKM", khuyenMaiService.findAll());
        model.addAttribute("pageTitle", "Quản lý khuyến mãi - UTC Cinema Admin");
        return "admin/quan-ly-khuyen-mai";
    }

    @PostMapping("/khuyen-mai/them")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String themKhuyenMai(
            @ModelAttribute KhuyenMai km,
            RedirectAttributes ra) {
        try {
            if (km.getTenKm() == null || km.getTenKm().isBlank())
                throw new BusinessException("Tên khuyến mãi không được để trống");
            if (km.getGiaTri() == null)
                throw new BusinessException("Giá trị không được để trống");
            khuyenMaiService.save(km);
            ra.addFlashAttribute("successMessage", "Thêm khuyến mãi thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }

    @PostMapping("/khuyen-mai/xoa/{maKm}")
    @PreAuthorize("hasRole('QUAN_LY')")
    public String xoaKhuyenMai(@PathVariable String maKm, RedirectAttributes ra) {
        try {
            khuyenMaiService.delete(maKm);
            ra.addFlashAttribute("successMessage", "Xóa khuyến mãi thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }

    // ===================================================================
    // QUAN LY THANH TOAN
    // ===================================================================

    @GetMapping("/thanh-toan")
    public String danhSachThanhToan(Model model) {
        model.addAttribute("danhSachTT", thanhToanRepository.findAllByOrderByNgayTtDesc());
        model.addAttribute("pageTitle", "Quản lý thanh toán - UTC Cinema Admin");
        return "admin/quan-ly-thanh-toan";
    }
}
