             -- ============================================================
-- Script tạo ghế cho tất cả phòng chiếu chưa có ghế
-- Chạy trong Oracle SQL Developer
-- Hàng A-E (mỗi hàng 10 ghế), tổng 48 ghế/phòng
-- ============================================================

DECLARE
    v_ma_ghe_seq NUMBER;
    v_ma_ghe     VARCHAR2(10);
    
    CURSOR c_phong IS
        SELECT p.MA_PHONG, p.SUC_CHUA
        FROM QL_RAP_PHIM.PHONG_CHIEU p
        WHERE NOT EXISTS (
            SELECT 1 FROM QL_RAP_PHIM.GHE g WHERE g.MA_PHONG = p.MA_PHONG
        );
        
    TYPE t_hang IS TABLE OF VARCHAR2(1);
    v_hangs t_hang := t_hang('A','B','C','D','E','F','G','H');
    
BEGIN
    -- Lấy số thứ tự ghế lớn nhất hiện tại
    SELECT NVL(MAX(TO_NUMBER(REPLACE(MA_GHE,'G',''))), 0) 
    INTO v_ma_ghe_seq 
    FROM QL_RAP_PHIM.GHE;
    
    FOR rec IN c_phong LOOP
        DBMS_OUTPUT.PUT_LINE('Tạo ghế cho phòng: ' || rec.MA_PHONG);
        
        -- Tạo 5 hàng, mỗi hàng 10 ghế (A-E: Thường/VIP/Đôi)
        FOR i IN 1..5 LOOP
            DECLARE
                v_hang   VARCHAR2(1) := v_hangs(i);
                v_loai   NVARCHAR2(30);
                v_so_ghe INTEGER;
            BEGIN
                -- Xác định loại ghế theo hàng
                IF i <= 2 THEN
                    v_loai := N'Thường';
                    v_so_ghe := 10;
                ELSIF i <= 4 THEN
                    v_loai := N'VIP';
                    v_so_ghe := 10;
                ELSE
                    v_loai := N'Đôi';
                    v_so_ghe := 8;
                END IF;
                
                FOR j IN 1..v_so_ghe LOOP
                    v_ma_ghe_seq := v_ma_ghe_seq + 1;
                    v_ma_ghe := 'G' || LPAD(v_ma_ghe_seq, 3, '0');
                    
                    INSERT INTO QL_RAP_PHIM.GHE (MA_GHE, MA_PHONG, HANG_GHE, SO_GHE, LOAI_GHE, TRANG_THAI)
                    VALUES (v_ma_ghe, rec.MA_PHONG, v_hang, j, v_loai, N'Hoạt động');
                END LOOP;
            END;
        END LOOP;
        
        DBMS_OUTPUT.PUT_LINE('  => Đã tạo 48 ghế cho phòng ' || rec.MA_PHONG);
    END LOOP;
    
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Hoàn tất! Đã tạo ghế cho tất cả phòng chiếu.');
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('LỖI: ' || SQLERRM);
        RAISE;
END;
/

-- Kiểm tra kết quả
SELECT p.MA_PHONG, p.TEN_PHONG, COUNT(g.MA_GHE) AS SO_GHE
FROM QL_RAP_PHIM.PHONG_CHIEU p
LEFT JOIN QL_RAP_PHIM.GHE g ON g.MA_PHONG = p.MA_PHONG
GROUP BY p.MA_PHONG, p.TEN_PHONG
ORDER BY p.MA_PHONG;
