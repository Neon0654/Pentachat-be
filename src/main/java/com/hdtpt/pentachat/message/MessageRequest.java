package com.hdtpt.pentachat.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {
    
    @NotNull(message = "from cannot be null")
    private String from;

    @NotNull(message = "to cannot be null")
    private String to;

    @NotBlank(message = "content cannot be blank")
    private String content;
}
