package com.julien.lotterysystem.entity.errorcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorCode {
    private Integer code;
    private String errMeg;
}
