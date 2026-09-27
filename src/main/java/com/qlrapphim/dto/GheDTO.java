package com.qlrapphim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO don gian de truyen thong tin ghe sang template Thymeleaf / JavaScript.
 * Tranh serialize Ghe entity truc tiep (co lazy relations gay loi).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GheDTO {
    private String maGhe;
    private String hangGhe;
    private Integer soGhe;
    private String loaiGhe;
    private String trangThai;
}
