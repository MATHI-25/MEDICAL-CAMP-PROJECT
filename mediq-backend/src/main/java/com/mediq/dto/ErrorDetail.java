package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private String field;
    private String message;

    public static ErrorDetail of(String field, String message) {
        return new ErrorDetail(field, message);
    }
}
