package com.prodpulse.prodpulse_backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogRequest {

    @NotBlank(message = "Logs cannot be empty")
    @Size(max = 2000, message = "Logs must be less than 2000 characters (approximately 150 words)")
    private String logs;

    private Object context; // accept SDK context, ignored in processing

}