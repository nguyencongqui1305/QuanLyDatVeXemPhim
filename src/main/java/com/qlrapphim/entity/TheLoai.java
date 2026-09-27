package com.qlrapphim.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import java.util.List;

@Entity
@Table(name = "THE_LOAI", schema = "QL_RAP_PHIM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheLoai {

    @Id
    @Column(name = "MA_THE_LOAI", length = 10)
    private String maTheLoai;

    @Nationalized
    @Column(name = "TEN_THE_LOAI", nullable = false, length = 80, unique = true)
    private String tenTheLoai;

    @JsonIgnore
    @ManyToMany(mappedBy = "theLoais", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Phim> phims;
}
