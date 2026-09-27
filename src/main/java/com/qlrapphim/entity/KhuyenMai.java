package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "KHUYEN_MAI", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhuyenMai {

    @Id
    @Column(name = "MA_KM", length = 10)
    private String maKm;

    @Nationalized
    @Column(name = "TEN_KM", nullable = false, length = 120)
    private String tenKm;

    // LOAI_GIAM: 'PERCENT' hoac 'AMOUNT'
    @Column(name = "LOAI_GIAM", nullable = false, length = 20)
    private String loaiGiam;

    @Column(name = "GIA_TRI", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "NGAY_BAT_DAU")
    private LocalDate ngayBatDau;

    @Column(name = "NGAY_KET_THUC")
    private LocalDate ngayKetThuc;

    @JsonIgnore
    @OneToMany(mappedBy = "khuyenMai", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DatVe> datVes;
}
