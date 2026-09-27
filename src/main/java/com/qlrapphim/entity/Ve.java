package com.qlrapphim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;

@Entity
@Table(name = "VE", schema = "QL_RAP_PHIM",
        uniqueConstraints = @UniqueConstraint(name = "UK_VE_LICH_GHE", columnNames = {"MA_LICH", "MA_GHE"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ve {

    @Id
    @Column(name = "MA_VE", length = 10)
    private String maVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_DAT", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DatVe datVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_LICH", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LichChieu lichChieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_GHE", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ghe ghe;

    @Column(name = "GIA_VE", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaVe;

    // Trang thai: 'Da dat', 'Da huy'
    @Nationalized
    @Column(name = "TRANG_THAI", length = 30)
    private String trangThai = "Đã đặt";
}
