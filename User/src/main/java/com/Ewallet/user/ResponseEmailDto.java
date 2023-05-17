package com.Ewallet.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseEmailDto {
    private String name;
    private String email;
}
