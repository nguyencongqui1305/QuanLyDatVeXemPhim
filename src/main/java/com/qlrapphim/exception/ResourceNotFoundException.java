package com.qlrapphim.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("Không tìm thấy %s với %s = '%s'", resourceName, field, value));
    }
}
