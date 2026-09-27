package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "LICH_CHIEU", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichChieu {

    @Id
    @Column(name = "MA_LICH", length = 10)
    private String maLich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_PHIM", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Phim phim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_PHONG", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PhongChieu phongChieu;

    @Column(name = "THOI_GIAN_BAT_DAU", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "THOI_GIAN_KET_THUC", nullable = false)
    private LocalDateTime thoiGianKetThuc;

    @Column(name = "GIA_VE", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaVe;

    @Nationalized
    @Column(name = "TRANG_THAI", length = 30)
    private String trangThai = "Mở bán";

    @JsonIgnore
    @OneToMany(mappedBy = "lichChieu", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Ve> ves;
}
