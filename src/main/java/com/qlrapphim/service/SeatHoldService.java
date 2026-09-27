package com.qlrapphim.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giu ghe tam thoi bang IN-MEMORY (thay the bang GHE_GIU_TAM trong DB).
 * Key: "maLich::maGhe"
 * Value: thong tin giu ghe (maKh, thoiGianHetHan)
 */
@Service
@Slf4j
public class SeatHoldService {

    private record HoldInfo(String maKh, LocalDateTime hetHan) {}

    /** Map<"maLich::maGhe", HoldInfo> */
    private final Map<String, HoldInfo> holds = new ConcurrentHashMap<>();

    @Value("${app.seat-hold-minutes:10}")
    private int seatHoldMinutes;

    // -------------------------------------------------------

    private String key(String maLich, String maGhe) {
        return maLich + "::" + maGhe;
    }

    /**
     * Giu ghe cho khach hang. Ghi de neu ghe dang bi giu boi chinh KH do.
     */
    public void hold(String maLich, String maGhe, String maKh) {
        holds.put(key(maLich, maGhe),
                new HoldInfo(maKh, LocalDateTime.now().plusMinutes(seatHoldMinutes)));
        log.debug("Giu ghe {} suất {} cho KH {} trong {} phut", maGhe, maLich, maKh, seatHoldMinutes);
    }

    /**
     * Kiem tra ghe co dang bi giu boi nguoi KHAC khong (con han).
     */
    public boolean isHeldByOther(String maLich, String maGhe, String maKhCurrent) {
        HoldInfo info = holds.get(key(maLich, maGhe));
        if (info == null) return false;
        if (info.hetHan().isBefore(LocalDateTime.now())) {
            holds.remove(key(maLich, maGhe)); // don dep luon
            return false;
        }
        return !info.maKh().equals(maKhCurrent);
    }

    /**
     * Giai phong tat ca ghe giu tam cua mot KH trong mot suat chieu.
     */
    public void release(String maLich, String maKh) {
        holds.entrySet().removeIf(e ->
                e.getKey().startsWith(maLich + "::") &&
                e.getValue().maKh().equals(maKh));
        log.debug("Giai phong ghe giu tam: KH {} suất {}", maKh, maLich);
    }

    /**
     * Scheduled: Don dep cac ghe giu tam het han (moi 1 phut).
     */
    @Scheduled(fixedDelay = 60_000)
    public void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        int before = holds.size();
        holds.entrySet().removeIf(e -> e.getValue().hetHan().isBefore(now));
        int removed = before - holds.size();
        if (removed > 0) {
            log.debug("Xoa {} ghe giu tam het han", removed);
        }
    }
}
