package com.github.accessreport.model.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

    private String error;
    private ZonedDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = ZonedDateTime.now();
    }

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = ZonedDateTime.now();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
