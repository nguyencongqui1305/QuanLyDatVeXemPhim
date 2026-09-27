package com.qlrapphim.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO tra ve cho trang xac nhan dat ve va ket qua thanh toan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatVeResponseDTO {
    private String maDat;
    private String tenPhim;
    private String tenPhong;
    private String tenRap;
    private LocalDateTime thoiGianChieu;
    private List<GheInfoDTO> gheDaDat;
    private BigDecimal tongTienGoc;
    private BigDecimal soTienGiam;
    private BigDecimal tongTienSauGiam;
    private String trangThai;
    private String maTt;  // Ma thanh toan

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GheInfoDTO {
        private String maVe;
        private String hangGhe;
        private Integer soGhe;
        private String loaiGhe;
        private BigDecimal giaVe;
    }
}
