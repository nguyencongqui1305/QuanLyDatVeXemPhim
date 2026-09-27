package com.qlrapphim.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de tra JSON an toan khi goi /phim/lich-chieu qua AJAX.
 * Tranh lazy loading exception khi Jackson serialize entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LichChieuDTO {
    private String maLich;
    private String maPhim;
    private String tenPhim;
    private String maPhong;
    private String tenPhong;
    private String maRap;
    private String tenRap;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime thoiGianBatDau;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime thoiGianKetThuc;
    private BigDecimal giaVe;
    private String trangThai;
}
