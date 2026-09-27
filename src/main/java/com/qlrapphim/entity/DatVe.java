package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DAT_VE", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatVe {

    @Id
    @Column(name = "MA_DAT", length = 10)
    private String maDat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_KH", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KhachHang khachHang;

    // Nhan vien co the null (dat ve online khong can nhan vien)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_NV")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private NhanVien nhanVien;

    // Khuyen mai co the null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_KM")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private KhuyenMai khuyenMai;

    @Column(name = "NGAY_DAT")
    private LocalDateTime ngayDat;

    @Column(name = "TONG_TIEN", precision = 12, scale = 2)
    private BigDecimal tongTien = BigDecimal.ZERO;

    // Trang thai: 'Cho thanh toan', 'Da thanh toan', 'Da huy'
    @Nationalized
    @Column(name = "TRANG_THAI", length = 30)
    private String trangThai = "Chờ thanh toán";

    @JsonIgnore
    @OneToMany(mappedBy = "datVe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Ve> ves;

    @JsonIgnore
    @OneToMany(mappedBy = "datVe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ThanhToan> thanhToans;

    @PrePersist
    protected void onCreate() {
        if (ngayDat == null) {
            ngayDat = LocalDateTime.now();
        }
    }
}
