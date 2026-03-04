package com.julien.lotterysystem.common.exception;

import com.julien.lotterysystem.entity.errorcode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
// 使用RuntimeException的equals、hashcode方法,因为lombok会重写
public class LotteryException extends RuntimeException{
    private Integer code;
    private String errMsg;

    public LotteryException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.errMsg = errorCode.getErrMeg();
    }

    public LotteryException(Integer code, String errMsg) {
        this.code = code;
        this.errMsg = errMsg;
    }

}
