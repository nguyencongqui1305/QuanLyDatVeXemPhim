package com.qlrapphim.controller.client;

import com.qlrapphim.dto.LichChieuDTO;
import com.qlrapphim.entity.LichChieu;
import com.qlrapphim.entity.Phim;
import com.qlrapphim.entity.RapChieu;
import com.qlrapphim.exception.ResourceNotFoundException;
import com.qlrapphim.service.LichChieuService;
import com.qlrapphim.service.PhimService;
import com.qlrapphim.service.RapChieuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/phim")
@RequiredArgsConstructor
@Slf4j
public class PhimController {

    private final PhimService phimService;
    private final LichChieuService lichChieuService;
    private final RapChieuService rapChieuService;

    @GetMapping("/{maPhim}")
    @Transactional(readOnly = true)
    public String chiTietPhim(@PathVariable String maPhim, Model model) {
        Phim phim = phimService.findById(maPhim)
                .orElseThrow(() -> new ResourceNotFoundException("Phim", "MA_PHIM", maPhim));

        // Force-initialize lazy collections WITHIN the transaction
        // to prevent LazyInitializationException during Thymeleaf rendering
        if (phim.getTheLoais() != null) {
            Hibernate.initialize(phim.getTheLoais());
            phim.getTheLoais().size(); // force load
        } else {
            phim.setTheLoais(new ArrayList<>());
        }

        List<RapChieu> danhSachRap = rapChieuService.findAll();

        model.addAttribute("phim", phim);
        model.addAttribute("danhSachRap", danhSachRap);
        model.addAttribute("ngayHienTai", LocalDate.now().toString());
        model.addAttribute("pageTitle", phim.getTenPhim() + " - UTC Cinema");
        return "client/phim-detail";
    }

    /**
     * Tra ve danh sach lich chieu theo phim + rap + ngay.
     * Dung LichChieuDTO thay vi entity de tranh Jackson lazy loading exception.
     */
    @GetMapping("/lich-chieu")
    @ResponseBody
    public List<LichChieuDTO> getLichChieu(
            @RequestParam String maPhim,
            @RequestParam String maRap,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {

        LocalDateTime ngayDt = ngay.atStartOfDay();
        List<LichChieu> lichChieus = lichChieuService.findByPhimAndRapAndNgay(maPhim, maRap, ngayDt);

        return lichChieus.stream()
                .map(lc -> LichChieuDTO.builder()
                        .maLich(lc.getMaLich())
                        .maPhim(lc.getPhim().getMaPhim())
                        .tenPhim(lc.getPhim().getTenPhim())
                        .maPhong(lc.getPhongChieu().getMaPhong())
                        .tenPhong(lc.getPhongChieu().getTenPhong())
                        .maRap(lc.getPhongChieu().getRapChieu().getMaRap())
                        .tenRap(lc.getPhongChieu().getRapChieu().getTenRap())
                        .thoiGianBatDau(lc.getThoiGianBatDau())
                        .thoiGianKetThuc(lc.getThoiGianKetThuc())
                        .giaVe(lc.getGiaVe())
                        .trangThai(lc.getTrangThai())
                        .build())
                .toList();
    }
}
