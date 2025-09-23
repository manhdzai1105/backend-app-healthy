package com.example.chat.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Date;
@Data
@Builder
public class ExceptionResponse {
    private Date timestamp;
    private String message;
    private  String details;
    private HttpStatus status;
}