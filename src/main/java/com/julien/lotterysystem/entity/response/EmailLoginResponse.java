package com.julien.lotterysystem.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginResponse {

    /** 是否已注册 */
    private Boolean registered;

    /** 用户ID（已注册时返回） */
    private Long id;
}
