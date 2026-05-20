package com.membership.external.dto;

import lombok.Data;

@Data
public class OpenResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> OpenResponse<T> success(T data) {
        OpenResponse<T> response = new OpenResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> OpenResponse<T> success() {
        return success(null);
    }

    public static <T> OpenResponse<T> error(String message) {
        OpenResponse<T> response = new OpenResponse<>();
        response.setCode(-1);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    public static <T> OpenResponse<T> error(int code, String message) {
        OpenResponse<T> response = new OpenResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}