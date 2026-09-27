package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.util.List;

@Entity
@Table(name = "PHONG_CHIEU", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhongChieu {

    @Id
    @Column(name = "MA_PHONG", length = 10)
    private String maPhong;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MA_RAP", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RapChieu rapChieu;

    @Nationalized
    @Column(name = "TEN_PHONG", nullable = false, length = 50)
    private String tenPhong;

    @Nationalized
    @Column(name = "LOAI_PHONG", length = 30)
    private String loaiPhong;

    @Column(name = "SUC_CHUA", nullable = false)
    private Integer sucChua;

    @JsonIgnore
    @OneToMany(mappedBy = "phongChieu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Ghe> ghes;

    @JsonIgnore
    @OneToMany(mappedBy = "phongChieu", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LichChieu> lichChieus;
}
