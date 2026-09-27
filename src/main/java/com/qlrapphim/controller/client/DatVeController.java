package com.qlrapphim.controller.client;

import com.qlrapphim.dto.DatVeRequestDTO;
import com.qlrapphim.dto.DatVeResponseDTO;
import com.qlrapphim.dto.GheDTO;
import com.qlrapphim.entity.*;
import com.qlrapphim.exception.ResourceNotFoundException;
import com.qlrapphim.security.UserPrincipal;
import com.qlrapphim.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dat-ve")
@RequiredArgsConstructor
@Slf4j
public class DatVeController {

    private final DatVeService datVeService;
    private final LichChieuService lichChieuService;
    private final GheService gheService;
    private final KhuyenMaiService khuyenMaiService;

    /**
     * Fallback khi truy cap /dat-ve/chon-ghe khong co maLich
     */
    @GetMapping("/chon-ghe")
    public String chonGheFallback() {
        return "redirect:/tim-kiem";
    }

    /**
     * Trang chon ghe - buoc dau tien cua quy trinh dat ve
     */
    @GetMapping("/chon-ghe/{maLich}")
    @PreAuthorize("isAuthenticated()")
    public String chonGhe(
            @PathVariable String maLich,
            @AuthenticationPrincipal UserPrincipal currentUser,
            Model model) {

        LichChieu lichChieu = lichChieuService.findById(maLich)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch chiếu", "MA_LICH", maLich));

        // Lay trang thai tung ghe (trong/da dat/giu tam)
        String maKhHienTai = "KHACH_HANG".equals(currentUser.getUserType()) ? currentUser.getUserId() : null;
        Map<String, String> trangThaiGhe = gheService.getTrangThaiGheForLich(maLich, maKhHienTai);

        // Lay danh sach ghe theo phong
        List<Ghe> danhSachGhe = gheService.findByPhong(lichChieu.getPhongChieu().getMaPhong());

        // Lay khuyen mai con hieu luc
        List<KhuyenMai> khuyenMais = khuyenMaiService.findHieuLuc();

        model.addAttribute("lichChieu", lichChieu);
        model.addAttribute("danhSachGhe", danhSachGhe);
        model.addAttribute("trangThaiGhe", trangThaiGhe);
        model.addAttribute("khuyenMais", khuyenMais);
        model.addAttribute("datVeRequest", new DatVeRequestDTO());
        model.addAttribute("seatHoldMinutes", 10);
        model.addAttribute("pageTitle", "Chọn ghế - " + lichChieu.getPhim().getTenPhim());
        return "client/chon-ghe";
    }

    /**
     * Giu ghe tam (duoc goi qua AJAX khi khach chon ghe va bam "Tiep tuc")
     */
    @PostMapping("/giu-ghe")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public Map<String, Object> giuGhe(
            @RequestParam String maLich,
            @RequestParam List<String> maGhes,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            gheService.giuGheTam(maLich, maGhes, currentUser.getUserId());
            return Map.of("success", true, "message", "Ghế đã được giữ tạm thời");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * Trang xac nhan va chon khuyen mai
     */
    @GetMapping("/xac-nhan")
    @PreAuthorize("isAuthenticated()")
    public String xacNhan(
            @RequestParam String maLich,
            @RequestParam List<String> maGhes,
            @RequestParam(required = false) String maKm,
            @AuthenticationPrincipal UserPrincipal currentUser,
            Model model) {

        LichChieu lichChieu = lichChieuService.getById(maLich);

        // Load ghe theo tung maGhe -> chuyen sang GheDTO (tranh serialize lazy entity)
        List<GheDTO> gheDaChon = maGhes.stream()
                .map(maGhe -> {
                    Ghe g = gheService.findById(maGhe);
                    return GheDTO.builder()
                            .maGhe(g.getMaGhe())
                            .hangGhe(g.getHangGhe())
                            .soGhe(g.getSoGhe())
                            .loaiGhe(g.getLoaiGhe() != null ? g.getLoaiGhe() : "Thường")
                            .trangThai(g.getTrangThai())
                            .build();
                })
                .toList();

        List<KhuyenMai> khuyenMais = khuyenMaiService.findHieuLuc();

        model.addAttribute("lichChieu", lichChieu);
        model.addAttribute("gheDaChon", gheDaChon);   // GheDTO - an toan cho th:inline="javascript"
        model.addAttribute("maGhes", maGhes);
        model.addAttribute("khuyenMais", khuyenMais);
        model.addAttribute("maKmChon", maKm);
        model.addAttribute("datVeRequest", DatVeRequestDTO.builder()
                .maLich(maLich)
                .maGhes(maGhes)
                .maKm(maKm)
                .build());
        model.addAttribute("pageTitle", "Xác nhận đặt vé - UTC Cinema");
        return "client/xac-nhan-dat-ve";
    }

    /**
     * Xu ly dat ve cuoi cung - POST from xac nhan form
     */
    @PostMapping("/thanh-toan")
    @PreAuthorize("isAuthenticated()")
    public String thanhToan(
            @Valid @ModelAttribute("datVeRequest") DatVeRequestDTO request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserPrincipal currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "client/xac-nhan-dat-ve";
        }

        try {
            String maKh = "KHACH_HANG".equals(currentUser.getUserType()) ? currentUser.getUserId() : null;
            String maNv = !"KHACH_HANG".equals(currentUser.getUserType()) ? currentUser.getUserId() : null;
            DatVeResponseDTO response = datVeService.datVe(request, maKh, maNv);
            redirectAttributes.addFlashAttribute("datVeResult", response);
            return "redirect:/dat-ve/ket-qua/" + response.getMaDat();
        } catch (Exception e) {
            log.error("Loi dat ve: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dat-ve/chon-ghe/" + request.getMaLich();
        }
    }

    /**
     * Trang ket qua dat ve (ve dien tu)
     */
    @GetMapping("/ket-qua/{maDat}")
    @PreAuthorize("isAuthenticated()")
    public String ketQua(
            @PathVariable String maDat,
            @AuthenticationPrincipal UserPrincipal currentUser,
            Model model) {

        DatVe datVe = datVeService.findById(maDat)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt vé", "MA_DAT", maDat));

        // Chi xem ket qua cua chinh minh (hoac la NV/QL)
        boolean isStaff = "NHAN_VIEN".equals(currentUser.getUserType());
        boolean isOwner = datVe.getKhachHang().getMaKh().equals(currentUser.getUserId());
        if (!isOwner && !isStaff) {
            return "redirect:/403";
        }

        model.addAttribute("datVe", datVe);
        model.addAttribute("pageTitle", "Đặt vé thành công - UTC Cinema");
        return "client/ket-qua-dat-ve";
    }

    /**
     * Lich su dat ve cua khach hang - ho tro filter theo trang thai
     */
    @GetMapping("/lich-su")
    @PreAuthorize("isAuthenticated()")
    public String lichSu(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String trangThai,
            Model model) {
        List<DatVe> danhSachDatVe = datVeService.findByKhachHang(currentUser.getUserId());

        // Filter theo trang thai neu co
        if (trangThai != null && !trangThai.isBlank()) {
            danhSachDatVe = danhSachDatVe.stream()
                    .filter(dv -> trangThai.equalsIgnoreCase(dv.getTrangThai()))
                    .toList();
        }

        model.addAttribute("danhSachDatVe", danhSachDatVe);
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("pageTitle", "Lịch sử đặt vé - UTC Cinema");
        return "client/lich-su-dat-ve";
    }

    /**
     * Huy dat ve
     */
    @PostMapping("/huy/{maDat}")
    @PreAuthorize("isAuthenticated()")
    public String huyDatVe(
            @PathVariable String maDat,
            @AuthenticationPrincipal UserPrincipal currentUser,
            RedirectAttributes redirectAttributes) {
        try {
            datVeService.huyDatVe(maDat, currentUser.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đặt vé thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dat-ve/lich-su";
    }
}
