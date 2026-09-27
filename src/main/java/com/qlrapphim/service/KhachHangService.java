package com.qlrapphim.service;

import com.qlrapphim.dto.RegisterDTO;
import com.qlrapphim.entity.KhachHang;
import com.qlrapphim.exception.BusinessException;
import com.qlrapphim.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;

    public List<KhachHang> findAll() {
        return khachHangRepository.findAll();
    }

    public Optional<KhachHang> findById(String maKh) {
        return khachHangRepository.findById(maKh);
    }

    public Optional<KhachHang> findByEmail(String email) {
        return khachHangRepository.findByEmail(email);
    }

    public Optional<KhachHang> findBySdt(String sdt) {
        return khachHangRepository.findBySdt(sdt);
    }

    @Transactional
    public KhachHang dangKy(RegisterDTO dto) {
        // Validate mat khau khop
        if (!dto.getMatKhau().equals(dto.getXacNhanMatKhau())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }
        // Kiem tra email trung
        if (khachHangRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email đã được sử dụng");
        }
        // Kiem tra SDT trung
        if (khachHangRepository.existsBySdt(dto.getSdt())) {
            throw new BusinessException("Số điện thoại đã được sử dụng");
        }

        KhachHang kh = KhachHang.builder()
                .maKh(generateMaKh())
                .hoTen(dto.getHoTen())
                .sdt(dto.getSdt())
                .email(dto.getEmail())
                .matKhau(passwordEncoder.encode(dto.getMatKhau()))
                .ngayDangKy(LocalDate.now())
                .build();

        return khachHangRepository.save(kh);
    }

    @Transactional
    public KhachHang save(KhachHang khachHang) {
        return khachHangRepository.save(khachHang);
    }

    @Transactional
    public void delete(String maKh) {
        khachHangRepository.deleteById(maKh);
    }

    private String generateMaKh() {
        long count = khachHangRepository.count() + 1;
        String maCandidate = String.format("KH%03d", count);
        while (khachHangRepository.existsById(maCandidate)) {
            count++;
            maCandidate = String.format("KH%03d", count);
        }
        return maCandidate;
    }
}
