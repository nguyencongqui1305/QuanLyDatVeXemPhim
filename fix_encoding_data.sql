-- ============================================================
-- FIX DU LIEU TRANG_THAI BI MOJIBAKE TRONG DB
-- ĐÃ LỖI THỜI: data.sql va DataInitializer.java hien da dung UTF-8 dung
-- chuan (NFC) ngay tu dau, script nay khong con can thiet cho du lieu
-- moi. Chi giu lai de tham khao neu tuong lai lai gap loi mojibake do
-- insert du lieu qua cong cu bi loi encoding (vd: sqlplus qua PowerShell).
-- Chay trong Oracle SQL Developer (ket noi voi user SYSTEM) neu can.
-- ============================================================

-- Kiem tra du lieu hien tai
SELECT MA_PHIM, TEN_PHIM, TRANG_THAI, LENGTH(TRANG_THAI) FROM QL_RAP_PHIM.PHIM ORDER BY MA_PHIM;

-- Fix PHIM table: reset TRANG_THAI ve gia tri dung dua tren pattern
-- Cac gia tri bi corrupt thuong co LENGTH > 15 hoac ky tu la
UPDATE QL_RAP_PHIM.PHIM
SET TRANG_THAI = N'Đang chiếu'
WHERE TRANG_THAI NOT IN (N'Đang chiếu', N'Sắp chiếu', N'Ngừng chiếu')
  AND (LOWER(DBMS_LOB.SUBSTR(TO_CLOB(TRANG_THAI), 20, 1)) LIKE '%ang%'
       OR TRANG_THAI IS NULL
       OR LENGTH(TRANG_THAI) = 0);

UPDATE QL_RAP_PHIM.PHIM
SET TRANG_THAI = N'Sắp chiếu'
WHERE TRANG_THAI NOT IN (N'Đang chiếu', N'Sắp chiếu', N'Ngừng chiếu')
  AND LOWER(DBMS_LOB.SUBSTR(TO_CLOB(TRANG_THAI), 20, 1)) LIKE '%sap%';

UPDATE QL_RAP_PHIM.PHIM
SET TRANG_THAI = N'Ngừng chiếu'
WHERE TRANG_THAI NOT IN (N'Đang chiếu', N'Sắp chiếu', N'Ngừng chiếu')
  AND LOWER(DBMS_LOB.SUBSTR(TO_CLOB(TRANG_THAI), 20, 1)) LIKE '%ng%';

-- Reset bat ky gia tri con lai chua khop
UPDATE QL_RAP_PHIM.PHIM
SET TRANG_THAI = N'Đang chiếu'
WHERE TRANG_THAI NOT IN (N'Đang chiếu', N'Sắp chiếu', N'Ngừng chiếu');

-- Tuong tu cho LICH_CHIEU
UPDATE QL_RAP_PHIM.LICH_CHIEU
SET TRANG_THAI = N'Mở bán'
WHERE TRANG_THAI NOT IN (N'Mở bán', N'Đã kết thúc', N'Tạm dừng');

-- Fix GHE table
UPDATE QL_RAP_PHIM.GHE
SET TRANG_THAI = N'Hoạt động'
WHERE TRANG_THAI NOT IN (N'Hoạt động', N'Bảo trì');

COMMIT;

-- Kiem tra ket qua sau khi fix
SELECT MA_PHIM, TEN_PHIM, TRANG_THAI FROM QL_RAP_PHIM.PHIM ORDER BY MA_PHIM;
