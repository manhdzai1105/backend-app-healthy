package com.example.chat.dto;

import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagingResponse<T> {
    private int code;
    private String message;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<T> data;
}

