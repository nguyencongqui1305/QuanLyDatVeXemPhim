package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "PHIM", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phim {

    @Id
    @Column(name = "MA_PHIM", length = 10)
    private String maPhim;

    @Nationalized
    @Column(name = "TEN_PHIM", nullable = false, length = 150)
    private String tenPhim;

    @Column(name = "THOI_LUONG", nullable = false)
    private Integer thoiLuong;

    @Nationalized
    @Column(name = "DAO_DIEN", length = 100)
    private String daoDien;

    @Column(name = "DO_TUOI", length = 10)
    private String doTuoi;

    @Column(name = "NGAY_KHOI_CHIEU")
    private LocalDate ngayKhoiChieu;

    @Nationalized
    @Column(name = "TRANG_THAI", length = 30)
    private String trangThai = "Đang chiếu";

    // Anh poster (phan mo rong: luu ten file anh trong static/images)
    @Column(name = "ANH_POSTER", length = 200)
    private String anhPoster;

    // Mo ta phim (phan mo rong: them truong mo ta)
    @Nationalized
    @Column(name = "MO_TA", length = 2000)
    private String moTa;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "PHIM_THE_LOAI",
            schema = "QL_RAP_PHIM",
            joinColumns = @JoinColumn(name = "MA_PHIM"),
            inverseJoinColumns = @JoinColumn(name = "MA_THE_LOAI")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TheLoai> theLoais;

    @JsonIgnore
    @OneToMany(mappedBy = "phim", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LichChieu> lichChieus;
}
