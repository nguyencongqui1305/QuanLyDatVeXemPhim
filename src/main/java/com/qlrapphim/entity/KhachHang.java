package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "KHACH_HANG", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {

    @Id
    @Column(name = "MA_KH", length = 10)
    private String maKh;

    @Nationalized
    @Column(name = "HO_TEN", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "SDT", nullable = false, length = 15, unique = true)
    private String sdt;

    @Column(name = "EMAIL", length = 100, unique = true)
    private String email;

    // Co them cot MAT_KHAU (mo rong so voi tai lieu goc) de luu BCrypt hash
    @Column(name = "MAT_KHAU", length = 200)
    private String matKhau;

    @Column(name = "NGAY_DANG_KY")
    private LocalDate ngayDangKy;

    @JsonIgnore
    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DatVe> datVes;
}
