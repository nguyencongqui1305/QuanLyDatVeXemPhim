package com.qlrapphim.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhimFormDTO {

    private String maPhim;

    @NotBlank(message = "Tên phim không được để trống")
    @Size(max = 150, message = "Tên phim tối đa 150 ký tự")
    private String tenPhim;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng phải lớn hơn 0")
    private Integer thoiLuong;

    @Size(max = 100, message = "Đạo diễn tối đa 100 ký tự")
    private String daoDien;

    // Dùng nullable pattern: cho phép null hoặc rỗng
    @Pattern(regexp = "^(P|K|T13|T16|T18)?$", message = "Độ tuổi không hợp lệ")
    private String doTuoi;

    private String ngayKhoiChieu;

    private String trangThai = "Đang chiếu";

    private String anhPoster;

    @Size(max = 2000)
    private String moTa;

    private List<String> maTheLoais;
}
