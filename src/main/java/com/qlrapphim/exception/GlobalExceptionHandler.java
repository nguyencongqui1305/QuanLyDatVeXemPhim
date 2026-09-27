package com.qlrapphim.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Không tìm thấy");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(GheDaDatException.class)
    public String handleGheDaDat(GheDaDatException ex, Model model) {
        log.warn("Seat already booked: {}", ex.getMessage());
        model.addAttribute("errorCode", "SEAT_TAKEN");
        model.addAttribute("errorTitle", "Ghế đã được đặt");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error";
    }

    // BusinessException KHÔNG được bắt ở đây.
    // Controller admin tự catch nó và redirect về danh sách với flash message.
    // Nếu bắt tại đây, Spring sẽ render trang error thay vì redirect.

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandler(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Trang không tồn tại");
        model.addAttribute("errorMessage", "Trang bạn tìm kiếm không tồn tại.");
        return "error/error";
    }

    /**
     * Bỏ qua lỗi static resource không tìm thấy (favicon.ico, *.css, *.js...).
     * Trả 404 im lặng, không log ERROR.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResource(NoResourceFoundException ex, Model model) {
        // Chỉ log debug thay vì error để không spam console
        log.debug("Static resource not found: {}", ex.getResourcePath());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Không tìm thấy");
        model.addAttribute("errorMessage", "Tài nguyên không tồn tại.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unexpected error: ", ex);
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Lỗi hệ thống");
        model.addAttribute("errorMessage", "Đã có lỗi xảy ra. Vui lòng thử lại sau.");
        return "error/error";
    }
}
