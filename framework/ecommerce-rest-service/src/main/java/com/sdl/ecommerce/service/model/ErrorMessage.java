package com.sdl.ecommerce.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Error Message
 *
 * @author nic
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    private String message;
    private String detail;
    
    public ErrorMessage(String message) {
        this.message = message;
    }

    public ErrorMessage(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }
}
