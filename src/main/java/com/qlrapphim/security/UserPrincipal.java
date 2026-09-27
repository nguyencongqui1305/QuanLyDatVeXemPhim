package com.qlrapphim.security;

import com.qlrapphim.entity.KhachHang;
import com.qlrapphim.entity.NhanVien;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation ho tro ca 2 loai user:
 * - KhachHang (role: ROLE_KHACH_HANG)
 * - NhanVien (role: ROLE_NHAN_VIEN hoac ROLE_QUAN_LY, lay tu CHUC_VU)
 */
public class UserPrincipal implements UserDetails {

    @Getter
    private final String userId;       // MA_KH hoac MA_NV
    @Getter
    private final String email;
    @Getter
    private final String hoTen;
    @Getter
    private final String userType;     // "KHACH_HANG" hoac "NHAN_VIEN"
    @Getter
    private final String chucVu;       // null cho khach hang, 'NHAN_VIEN'/'QUAN_LY' cho nhan vien
    private final String matKhau;
    private final boolean accountNonLocked;
    private final Collection<? extends GrantedAuthority> authorities;

    // Constructor cho KhachHang
    public UserPrincipal(KhachHang khachHang) {
        this.userId = khachHang.getMaKh();
        this.email = khachHang.getEmail();
        this.hoTen = khachHang.getHoTen();
        this.matKhau = khachHang.getMatKhau();
        this.userType = "KHACH_HANG";
        this.chucVu = null;
        this.accountNonLocked = true;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG"));
    }

    // Constructor cho NhanVien
    public UserPrincipal(NhanVien nhanVien) {
        this.userId = nhanVien.getMaNv();
        this.email = nhanVien.getEmail();
        this.hoTen = nhanVien.getHoTen();
        this.matKhau = nhanVien.getMatKhau();
        this.userType = "NHAN_VIEN";
        this.chucVu = nhanVien.getChucVu();
        this.accountNonLocked = !"Đã khóa".equals(nhanVien.getTrangThai());
        // Anh xa CHUC_VU -> Spring Security ROLE
        String role = "QUAN_LY".equals(nhanVien.getChucVu()) ? "ROLE_QUAN_LY" : "ROLE_NHAN_VIEN";
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return matKhau;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
