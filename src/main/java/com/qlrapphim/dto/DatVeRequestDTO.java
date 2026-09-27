package com.qlrapphim.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatVeRequestDTO {

    @NotBlank(message = "Mã lịch chiếu không được để trống")
    private String maLich;

    @NotEmpty(message = "Vui lòng chọn ít nhất 1 ghế")
    private List<String> maGhes;

    private String maKm;  // Mã khuyến mãi (có thể null)

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String phuongThucThanhToan;
}
