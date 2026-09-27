package com.qlrapphim.repository;

import com.qlrapphim.entity.TheLoai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TheLoaiRepository extends JpaRepository<TheLoai, String> {
    List<TheLoai> findAllByOrderByTenTheLoaiAsc();
}
