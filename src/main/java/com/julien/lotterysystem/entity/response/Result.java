package com.julien.lotterysystem.entity.response;

import com.julien.lotterysystem.entity.errorcode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // 返回成功结果
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    // 返回失败结果
    public static <T> Result<T> fail(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getErrMeg());
        return result;
    }


    public static <T> Result<T> fail(Integer code,String errMsg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(errMsg);
        return result;
    }
}
