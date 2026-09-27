package com.qlrapphim.controller.admin;

import com.qlrapphim.dto.PhimFormDTO;
import com.qlrapphim.entity.Phim;
import com.qlrapphim.entity.TheLoai;
import com.qlrapphim.exception.BusinessException;
import com.qlrapphim.exception.ResourceNotFoundException;
import com.qlrapphim.service.PhimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/phim")
@PreAuthorize("hasRole('QUAN_LY')")
@RequiredArgsConstructor
@Slf4j
public class AdminPhimController {

    private final PhimService phimService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping
    public String danhSach(Model model) {
        List<Phim> danhSach = phimService.findAll();
        model.addAttribute("danhSachPhim", danhSach);
        model.addAttribute("pageTitle", "Quản lý phim - UTC Cinema Admin");
        return "admin/quan-ly-phim";
    }

    @GetMapping("/them")
    public String themPhimForm(Model model) {
        List<TheLoai> danhSachTheLoai = phimService.findAllTheLoai();
        model.addAttribute("phimForm", new PhimFormDTO());
        model.addAttribute("danhSachTheLoai", danhSachTheLoai);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm phim - UTC Cinema Admin");
        return "admin/phim-form";
    }

    @PostMapping("/them")
    public String themPhim(
            @Valid @ModelAttribute("phimForm") PhimFormDTO dto,
            BindingResult bindingResult,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachTheLoai", phimService.findAllTheLoai());
            model.addAttribute("isEdit", false);
            return "admin/phim-form";
        }

        try {
            // Xử lý upload ảnh poster nếu có file mới
            String savedFileName = savePosterFile(posterFile, dto.getAnhPoster());
            dto.setAnhPoster(savedFileName);

            Phim phim = buildPhimFromDTO(dto, null);
            phimService.save(phim);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phim thành công!");
        } catch (BusinessException e) {
            log.warn("[AdminPhimController] BusinessException khi thêm phim: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("[AdminPhimController] Lỗi khi thêm phim: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/phim";
    }

    @GetMapping("/sua/{maPhim}")
    public String suaPhimForm(@PathVariable String maPhim, Model model) {
        Phim phim = phimService.findById(maPhim)
                .orElseThrow(() -> new ResourceNotFoundException("Phim", "MA_PHIM", maPhim));

        PhimFormDTO dto = PhimFormDTO.builder()
                .maPhim(phim.getMaPhim())
                .tenPhim(phim.getTenPhim())
                .thoiLuong(phim.getThoiLuong())
                .daoDien(phim.getDaoDien())
                .doTuoi(phim.getDoTuoi())
                .ngayKhoiChieu(phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().toString() : null)
                .trangThai(phim.getTrangThai())
                .moTa(phim.getMoTa())
                .anhPoster(phim.getAnhPoster())
                .maTheLoais(phim.getTheLoais().stream().map(TheLoai::getMaTheLoai).toList())
                .build();

        model.addAttribute("phimForm", dto);
        model.addAttribute("danhSachTheLoai", phimService.findAllTheLoai());
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Sửa phim - UTC Cinema Admin");
        return "admin/phim-form";
    }

    @PostMapping("/sua/{maPhim}")
    public String suaPhim(
            @PathVariable String maPhim,
            @Valid @ModelAttribute("phimForm") PhimFormDTO dto,
            BindingResult bindingResult,
            @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachTheLoai", phimService.findAllTheLoai());
            model.addAttribute("isEdit", true);
            return "admin/phim-form";
        }

        try {
            // Xử lý upload ảnh poster nếu có file mới
            String savedFileName = savePosterFile(posterFile, dto.getAnhPoster());
            dto.setAnhPoster(savedFileName);

            Phim phim = buildPhimFromDTO(dto, maPhim);
            phimService.save(phim);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phim thành công!");
        } catch (BusinessException e) {
            log.warn("[AdminPhimController] BusinessException khi sửa phim {}: {}", maPhim, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("[AdminPhimController] Lỗi khi sửa phim {}: {}", maPhim, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/phim";
    }

    @PostMapping("/xoa/{maPhim}")
    public String xoaPhim(@PathVariable String maPhim, RedirectAttributes redirectAttributes) {
        try {
            phimService.delete(maPhim);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phim thành công!");
        } catch (BusinessException e) {
            // Lỗi nghiệp vụ (vd: phim đã có lịch chiếu) - hiển thị thông báo rõ ràng
            log.warn("[AdminPhimController] Không thể xóa phim {}: {}", maPhim, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("[AdminPhimController] Lỗi hệ thống khi xóa phim {}: {}", maPhim, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/phim";
    }

    /**
     * Lưu file poster vào thư mục uploads/images/posters/.
     * Tên file được đổi thành UUID để tránh trùng.
     * Nếu không có file mới, giữ nguyên tên ảnh cũ (existingPoster).
     */
    private String savePosterFile(MultipartFile file, String existingPoster) throws IOException {
        if (file == null || file.isEmpty()) {
            return existingPoster; // Giữ ảnh cũ nếu không upload mới
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "poster";
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : ".jpg";
        String newFileName = UUID.randomUUID().toString() + ext;

        Path uploadPath = Paths.get(uploadDir, "images", "posters").toAbsolutePath();
        Files.createDirectories(uploadPath); // Tự tạo thư mục nếu chưa có
        Files.copy(file.getInputStream(), uploadPath.resolve(newFileName));

        log.info("[AdminPhimController] Đã upload poster: {} -> {}", originalName, uploadPath.resolve(newFileName));
        return newFileName;
    }

    private Phim buildPhimFromDTO(PhimFormDTO dto, String maPhim) {
        // Load TheLoai THẬT từ DB (managed entity) thay vì tạo detached object mới.
        // Nếu dùng detached entity, Hibernate merge qua connection pool khác
        // có thể không inherit oracle.jdbc.defaultNChar=true → gây Mojibake.
        List<TheLoai> allTheLoai = phimService.findAllTheLoai();
        List<TheLoai> theLoaiList = dto.getMaTheLoais() != null
                ? allTheLoai.stream()
                    .filter(tl -> dto.getMaTheLoais().contains(tl.getMaTheLoai()))
                    .toList()
                : List.of();

        // Normalize doTuoi: null/blank -> null
        String doTuoi = (dto.getDoTuoi() != null && !dto.getDoTuoi().isBlank()) ? dto.getDoTuoi() : null;

        // Map trang thai sang gia tri Unicode chinh xac
        String trangThai = mapTrangThai(dto.getTrangThai());

        Phim phim = Phim.builder()
                .maPhim(maPhim)
                .tenPhim(dto.getTenPhim())
                .thoiLuong(dto.getThoiLuong())
                .daoDien(dto.getDaoDien())
                .doTuoi(doTuoi)
                .ngayKhoiChieu(dto.getNgayKhoiChieu() != null && !dto.getNgayKhoiChieu().isBlank()
                        ? LocalDate.parse(dto.getNgayKhoiChieu()) : null)
                .trangThai(trangThai)
                .moTa(dto.getMoTa())
                .anhPoster(dto.getAnhPoster())
                .theLoais(theLoaiList)
                .build();
        return phim;
    }

    /**
     * Map ASCII code từ HTML form sang chuỗi tiếng Việt hardcoded.
     * Form gửi: "dang-chieu" | "sap-chieu" | "ngung-chieu"
     * Không bao giờ lưu input gốc (có thể bị Mojibake) vào DB.
     */
    private String mapTrangThai(String input) {
        if (input == null || input.isBlank()) return "Đang chiếu";
        return switch (input.trim().toLowerCase()) {
            case "dang-chieu" -> "Đang chiếu";
            case "sap-chieu"  -> "Sắp chiếu";
            case "ngung-chieu" -> "Ngừng chiếu";
            // Fallback cho data cũ hoặc edit form gửi lại value cũ
            default -> {
                String lo = input.toLowerCase();
                if (lo.contains("sap") || lo.contains("sắp") || (lo.contains("s") && lo.contains("p chi")))
                    yield "Sắp chiếu";
                if (lo.contains("ngung") || lo.contains("ngừng") || lo.contains("ng chi"))
                    yield "Ngừng chiếu";
                yield "Đang chiếu"; // safe fallback
            }
        };
    }
}
