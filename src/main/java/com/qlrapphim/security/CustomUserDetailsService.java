package com.qlrapphim.security;

import com.qlrapphim.entity.KhachHang;
import com.qlrapphim.entity.NhanVien;
import com.qlrapphim.repository.KhachHangRepository;
import com.qlrapphim.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Custom UserDetailsService: load user by email.
 * Uu tien tim trong NhanVien truoc, neu khong co thi tim trong KhachHang.
 * Dieu nay cho phep nhan vien va khach hang dung cung endpoint dang nhap.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        // Tim trong NhanVien truoc
        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(email);
        if (nhanVienOpt.isPresent()) {
            log.debug("Found NhanVien: {}", email);
            return new UserPrincipal(nhanVienOpt.get());
        }

        // Tim trong KhachHang
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByEmail(email);
        if (khachHangOpt.isPresent()) {
            log.debug("Found KhachHang: {}", email);
            return new UserPrincipal(khachHangOpt.get());
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email);
    }
}
