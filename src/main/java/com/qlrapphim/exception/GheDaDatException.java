package com.qlrapphim.exception;

/**
 * Exception khi khach hang co ghe da bi dat trong suat chieu.
 * Tuong ung logic nghiep vu PROC_THEM_VE trong tai lieu.
 */
public class GheDaDatException extends RuntimeException {
    public GheDaDatException(String message) {
        super(message);
    }
}
