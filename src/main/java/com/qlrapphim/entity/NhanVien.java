package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.util.List;

@Entity
@Table(name = "NHAN_VIEN", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @Column(name = "MA_NV", length = 10)
    private String maNv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_RAP", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RapChieu rapChieu;

    @Nationalized
    @Column(name = "HO_TEN", nullable = false, length = 100)
    private String hoTen;

    // CHUC_VU: 'NHAN_VIEN' hoac 'QUAN_LY' - anh xa sang Spring Security role
    @Nationalized
    @Column(name = "CHUC_VU", length = 50)
    private String chucVu;

    @Column(name = "SDT", length = 15)
    private String sdt;

    @Column(name = "EMAIL", length = 100, unique = true)
    private String email;

    // Co them cot MAT_KHAU (mo rong) de luu BCrypt hash cho dang nhap
    @Column(name = "MAT_KHAU", length = 200)
    private String matKhau;

    // Trang thai tai khoan: 'Hoạt động' hoac 'Đã khóa'
    @Nationalized
    @Column(name = "TRANG_THAI", length = 30)
    private String trangThai = "Hoạt động";

    @JsonIgnore
    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DatVe> datVes;
}
