package com.qlrapphim.config;

import com.qlrapphim.entity.*;
import com.qlrapphim.repository.*;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Khởi tạo dữ liệu mẫu khi app start (nếu DB chưa có data).
 * Dùng Java String (Unicode) để tránh lỗi encoding khi đọc SQL file.
 *
 * Tự động kiểm tra: nếu DB đã có data thì bỏ qua, không insert thêm.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RapChieuRepository rapChieuRepository;
    private final PhongChieuRepository phongChieuRepository;
    private final TheLoaiRepository theLoaiRepository;
    private final PhimRepository phimRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final GheRepository gheRepository;
    private final LichChieuRepository lichChieuRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (rapChieuRepository.count() > 0) {
            log.info("Dữ liệu đã tồn tại, bỏ qua khởi tạo.");
            // Vẫn kiểm tra và tạo ghế cho phòng nào đang thiếu
            generateMissingSeats();
            // Fix phim chưa có độ tuổi - dùng JDBC riêng để tránh conflict transaction
            fixMissingDoTuoi();
            return;
        }
        log.info("DB trống — đang khởi tạo dữ liệu mẫu...");
        insertRap();
        insertTheLoai();
        insertPhim();
        insertKhachHang();
        insertNhanVien();
        insertKhuyenMai();
        generateMissingSeats();
        log.info("Khởi tạo dữ liệu mẫu hoàn tất.");
    }

    /**
     * Tự động tạo ghế cho các phòng chiếu đang thiếu ghế.
     * Chạy mỗi lần app khởi động để fix dữ liệu cũ.
     */
    private void generateMissingSeats() {
        List<PhongChieu> phongs = phongChieuRepository.findAll();
        int totalCreated = 0;

        for (PhongChieu phong : phongs) {
            long soCho = gheRepository.countByPhongChieuMaPhong(phong.getMaPhong());
            if (soCho == 0) {
                log.info("Phòng {} ({}) chưa có ghế — tạo ghế tự động...",
                        phong.getMaPhong(), phong.getTenPhong());
                generateGheForPhong(phong);
                totalCreated++;
            }
        }

        if (totalCreated > 0) {
            log.info("Đã tạo ghế cho {} phòng chiếu thiếu ghế.", totalCreated);
        } else {
            log.info("Tất cả phòng chiếu đã có ghế.");
        }
    }

    /**
     * Auto-fix: đảm bảo cột DO_TUOI tồn tại và set giá trị mặc định.
     * Dùng raw JDBC để tránh conflict với @Transactional của JPA.
     */
    private void fixMissingDoTuoi() {
        try (java.sql.Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);

            // Bước 1: Đảm bảo cột DO_TUOI tồn tại
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE QL_RAP_PHIM.PHIM ADD (DO_TUOI VARCHAR2(10 CHAR))");
                log.info("Đã tạo cột DO_TUOI trong bảng PHIM.");
            } catch (java.sql.SQLException e) {
                // ORA-01430: column being added already exists -> OK
                log.debug("Cột DO_TUOI đã tồn tại (OK).");
            }

            // Bước 2: UPDATE tất cả phim chưa có doTuoi
            try (java.sql.Statement stmt = conn.createStatement()) {
                int updated = stmt.executeUpdate(
                    "UPDATE QL_RAP_PHIM.PHIM SET DO_TUOI = 'P' WHERE DO_TUOI IS NULL");
                if (updated > 0) {
                    log.info("Đã fix doTuoi='P' cho {} phim bằng JDBC.", updated);
                } else {
                    log.info("Tất cả phim đã có doTuoi.");
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi fix doTuoi: {}", e.getMessage());
        }
    }

    /**
     * Tạo ghế cho một phòng chiếu: A/B=Thường(10), C/D=VIP(10), E=Đôi(8) = 48 ghế
     */
    private void generateGheForPhong(PhongChieu phong) {
        long maxGheId = gheRepository.count();

        String[][] layout = {
            {"A", "Thường", "10"},
            {"B", "Thường", "10"},
            {"C", "VIP",    "10"},
            {"D", "VIP",    "10"},
            {"E", "Đôi",    "8"}
        };

        for (String[] row : layout) {
            String hang     = row[0];
            String loaiGhe  = row[1];
            int    soGheMax = Integer.parseInt(row[2]);
            for (int soGhe = 1; soGhe <= soGheMax; soGhe++) {
                maxGheId++;
                String maGhe = String.format("G%03d", maxGheId);
                // Tránh trùng mã ghế
                while (gheRepository.existsById(maGhe)) {
                    maxGheId++;
                    maGhe = String.format("G%03d", maxGheId);
                }
                Ghe g = Ghe.builder()
                        .maGhe(maGhe)
                        .phongChieu(phong)
                        .hangGhe(hang)
                        .soGhe(soGhe)
                        .loaiGhe(loaiGhe)
                        .trangThai("Hoạt động")
                        .build();
                gheRepository.save(g);
            }
        }
        log.info("  -> Tạo 48 ghế cho phòng {}", phong.getMaPhong());
    }

    private void insertRap() {
        List<RapChieu> raps = List.of(
            RapChieu.builder().maRap("R001").tenRap("UTC Cinema Hà Nội")
                .diaChi("123 Nguyễn Trãi, Thanh Xuân, Hà Nội").sdt("0241234567").build(),
            RapChieu.builder().maRap("R002").tenRap("UTC Cinema HCM")
                .diaChi("456 Lê Văn Việt, Quận 9, TP.HCM").sdt("0281234567").build(),
            RapChieu.builder().maRap("R003").tenRap("UTC Cinema Đà Nẵng")
                .diaChi("789 Nguyễn Văn Linh, Đà Nẵng").sdt("0236123456").build(),
            RapChieu.builder().maRap("R004").tenRap("UTC Cinema Cần Thơ")
                .diaChi("321 30 Tháng 4, Ninh Kiều, Cần Thơ").sdt("0292123456").build()
        );
        rapChieuRepository.saveAll(raps);

        // Phòng chiếu R001
        RapChieu r001 = raps.get(0);
        List<PhongChieu> phongs = List.of(
            PhongChieu.builder().maPhong("P001").rapChieu(r001).tenPhong("Phòng 1").loaiPhong("2D").sucChua(80).build(),
            PhongChieu.builder().maPhong("P002").rapChieu(r001).tenPhong("Phòng 2").loaiPhong("3D").sucChua(60).build(),
            PhongChieu.builder().maPhong("P003").rapChieu(r001).tenPhong("Phòng VIP").loaiPhong("4DX").sucChua(40).build(),
            PhongChieu.builder().maPhong("P004").rapChieu(raps.get(1)).tenPhong("Phòng 1").loaiPhong("2D").sucChua(100).build(),
            PhongChieu.builder().maPhong("P005").rapChieu(raps.get(2)).tenPhong("Phòng 1").loaiPhong("IMAX").sucChua(120).build()
        );
        phongChieuRepository.saveAll(phongs);

        // Ghế cho P001
        PhongChieu p001 = phongs.get(0);
        String[] hangs = {"A","B","C","D","E"};
        int gheId = 1;
        for (String hang : hangs) {
            String loai = hang.compareTo("C") < 0 ? "Thường" : (hang.equals("E") ? "Đôi" : "VIP");
            int soCho = hang.equals("E") ? 8 : 10;
            for (int so = 1; so <= soCho; so++) {
                Ghe g = Ghe.builder()
                    .maGhe(String.format("G%03d", gheId++))
                    .phongChieu(p001).hangGhe(hang).soGhe(so)
                    .loaiGhe(loai).trangThai("Hoạt động").build();
                gheRepository.save(g);
            }
        }
    }

    private void insertTheLoai() {
        List<TheLoai> theLoais = List.of(
            TheLoai.builder().maTheLoai("TL001").tenTheLoai("Hành động").build(),
            TheLoai.builder().maTheLoai("TL002").tenTheLoai("Hài hước").build(),
            TheLoai.builder().maTheLoai("TL003").tenTheLoai("Kinh dị").build(),
            TheLoai.builder().maTheLoai("TL004").tenTheLoai("Tình cảm").build(),
            TheLoai.builder().maTheLoai("TL005").tenTheLoai("Hoạt hình").build(),
            TheLoai.builder().maTheLoai("TL006").tenTheLoai("Khoa học viễn tưởng").build(),
            TheLoai.builder().maTheLoai("TL007").tenTheLoai("Phiêu lưu").build(),
            TheLoai.builder().maTheLoai("TL008").tenTheLoai("Tâm lý").build()
        );
        theLoaiRepository.saveAll(theLoais);
    }

    private void insertPhim() {
        TheLoai tl001 = theLoaiRepository.findById("TL001").orElseThrow();
        TheLoai tl002 = theLoaiRepository.findById("TL002").orElseThrow();
        TheLoai tl004 = theLoaiRepository.findById("TL004").orElseThrow();
        TheLoai tl005 = theLoaiRepository.findById("TL005").orElseThrow();
        TheLoai tl006 = theLoaiRepository.findById("TL006").orElseThrow();
        TheLoai tl007 = theLoaiRepository.findById("TL007").orElseThrow();

        Phim ph001 = Phim.builder().maPhim("PH001").tenPhim("Avengers: Secret Wars")
            .thoiLuong(150).daoDien("Joe Russo").doTuoi("T13")
            .ngayKhoiChieu(LocalDate.of(2026,5,1)).trangThai("Đang chiếu")
            .moTa("Cuộc chiến cuối cùng của các siêu anh hùng.").theLoais(List.of(tl001, tl006)).build();

        Phim ph002 = Phim.builder().maPhim("PH002").tenPhim("Lật Mặt 8")
            .thoiLuong(120).daoDien("Lý Hải").doTuoi("P")
            .ngayKhoiChieu(LocalDate.of(2026,4,15)).trangThai("Đang chiếu")
            .moTa("Bộ phim hài hành động đặc sắc của đạo diễn Lý Hải.").theLoais(List.of(tl002, tl004)).build();

        Phim ph003 = Phim.builder().maPhim("PH003").tenPhim("Inside Out 3")
            .thoiLuong(95).daoDien("Pete Docter").doTuoi("P")
            .ngayKhoiChieu(LocalDate.of(2026,6,20)).trangThai("Sắp chiếu")
            .moTa("Hành trình khám phá cảm xúc của Riley.").theLoais(List.of(tl002, tl005)).build();

        Phim ph004 = Phim.builder().maPhim("PH004").tenPhim("Venom: The Last Dance")
            .thoiLuong(110).daoDien("Kelly Marcel").doTuoi("T16")
            .ngayKhoiChieu(LocalDate.of(2026,3,10)).trangThai("Đang chiếu")
            .moTa("Cuộc phiêu lưu cuối cùng của Eddie Brock và Venom.").theLoais(List.of(tl001)).build();

        Phim ph005 = Phim.builder().maPhim("PH005").tenPhim("Moana 2")
            .thoiLuong(100).daoDien("David Derrick Jr.").doTuoi("P")
            .ngayKhoiChieu(LocalDate.of(2026,7,1)).trangThai("Sắp chiếu")
            .moTa("Moana trở lại trong hành trình mới khám phá đại dương.").theLoais(List.of(tl005, tl007)).build();

        phimRepository.saveAll(List.of(ph001, ph002, ph003, ph004, ph005));

        // Lịch chiếu
        PhongChieu p001 = phongChieuRepository.findById("P001").orElseThrow();
        PhongChieu p002 = phongChieuRepository.findById("P002").orElseThrow();
        LichChieu lc1 = LichChieu.builder().maLich("LC001").phim(ph001).phongChieu(p001)
            .thoiGianBatDau(LocalDateTime.of(2026,6,25,9,0))
            .thoiGianKetThuc(LocalDateTime.of(2026,6,25,11,30))
            .giaVe(new BigDecimal("85000")).trangThai("Mở bán").build();
        LichChieu lc2 = LichChieu.builder().maLich("LC002").phim(ph002).phongChieu(p001)
            .thoiGianBatDau(LocalDateTime.of(2026,6,25,14,0))
            .thoiGianKetThuc(LocalDateTime.of(2026,6,25,16,0))
            .giaVe(new BigDecimal("75000")).trangThai("Mở bán").build();
        LichChieu lc3 = LichChieu.builder().maLich("LC003").phim(ph001).phongChieu(p002)
            .thoiGianBatDau(LocalDateTime.of(2026,6,25,19,0))
            .thoiGianKetThuc(LocalDateTime.of(2026,6,25,21,30))
            .giaVe(new BigDecimal("120000")).trangThai("Mở bán").build();
        lichChieuRepository.saveAll(List.of(lc1, lc2, lc3));
    }

    private void insertKhachHang() {
        String bcrypt = passwordEncoder.encode("123456");
        List<KhachHang> khs = List.of(
            KhachHang.builder().maKh("KH001").hoTen("Nguyễn Văn An").sdt("0901234567")
                .email("an.nguyen@email.com").matKhau(bcrypt)
                .ngayDangKy(LocalDate.of(2024,1,15)).build(),
            KhachHang.builder().maKh("KH002").hoTen("Trần Thị Bình").sdt("0912345678")
                .email("binh.tran@email.com").matKhau(bcrypt)
                .ngayDangKy(LocalDate.of(2024,2,20)).build(),
            KhachHang.builder().maKh("KH003").hoTen("Lê Minh Cường").sdt("0923456789")
                .email("cuong.le@email.com").matKhau(bcrypt)
                .ngayDangKy(LocalDate.of(2024,3,10)).build()
        );
        khachHangRepository.saveAll(khs);
    }

    private void insertNhanVien() {
        String bcrypt = passwordEncoder.encode("123456");
        RapChieu r001 = rapChieuRepository.findById("R001").orElseThrow();
        RapChieu r002 = rapChieuRepository.findById("R002").orElseThrow();
        List<NhanVien> nvs = List.of(
            NhanVien.builder().maNv("NV001").rapChieu(r001).hoTen("Đinh Quản Lý")
                .chucVu("QUAN_LY").sdt("0211111111")
                .email("quanly@utccinema.vn").matKhau(bcrypt).build(),
            NhanVien.builder().maNv("NV002").rapChieu(r001).hoTen("Vũ Bán Vé")
                .chucVu("NHAN_VIEN").sdt("0222222222")
                .email("banve@utccinema.vn").matKhau(bcrypt).build(),
            NhanVien.builder().maNv("NV003").rapChieu(r002).hoTen("Ngô Thị Lan")
                .chucVu("NHAN_VIEN").sdt("0233333333")
                .email("lan.ngo@utccinema.vn").matKhau(bcrypt).build()
        );
        nhanVienRepository.saveAll(nvs);
    }

    private void insertKhuyenMai() {
        List<KhuyenMai> kms = List.of(
            KhuyenMai.builder().maKm("KM001").tenKm("Giảm 20% dịp hè")
                .loaiGiam("PERCENT").giaTri(new BigDecimal("20"))
                .ngayBatDau(LocalDate.of(2026,6,1)).ngayKetThuc(LocalDate.of(2026,8,31)).build(),
            KhuyenMai.builder().maKm("KM002").tenKm("Giảm 50.000đ sinh viên")
                .loaiGiam("AMOUNT").giaTri(new BigDecimal("50000"))
                .ngayBatDau(LocalDate.of(2026,1,1)).ngayKetThuc(LocalDate.of(2026,12,31)).build(),
            KhuyenMai.builder().maKm("KM003").tenKm("Flash sale cuối tuần")
                .loaiGiam("PERCENT").giaTri(new BigDecimal("30"))
                .ngayBatDau(LocalDate.of(2026,6,14)).ngayKetThuc(LocalDate.of(2026,8,31)).build()
        );
        khuyenMaiRepository.saveAll(kms);
    }
}
