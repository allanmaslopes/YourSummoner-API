package com.your.summoner.api.model;

import org.springframework.http.HttpStatus;

public class Status {

    private String message;

    private Integer status_code;

    private HttpStatus httpStatus;

    public Status(String message, Integer status_code) {
        this.message = message;
        this.status_code = status_code;
        this.httpStatus = HttpStatus.resolve(status_code);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus_code() {
        return status_code;
    }

    public void setStatus_code(Integer status_code) {
        this.status_code = status_code;
    }

    public HttpStatus getHttpStatus() {

        if (httpStatus == null && status_code != null) {
            this.httpStatus = HttpStatus.resolve(this.status_code);
        }

        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
