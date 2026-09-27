package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.util.List;

@Entity
@Table(name = "RAP_CHIEU", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RapChieu {

    @Id
    @Column(name = "MA_RAP", length = 10)
    private String maRap;

    @Nationalized
    @Column(name = "TEN_RAP", nullable = false, length = 100)
    private String tenRap;

    @Nationalized
    @Column(name = "DIA_CHI", nullable = false, length = 200)
    private String diaChi;

    @Column(name = "SDT", length = 15, unique = true)
    private String sdt;

    @JsonIgnore
    @OneToMany(mappedBy = "rapChieu", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PhongChieu> phongChieus;

    @JsonIgnore
    @OneToMany(mappedBy = "rapChieu", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<NhanVien> nhanViens;
}
