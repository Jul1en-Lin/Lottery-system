package com.julien.lotterysystem.common.advice;

import com.julien.lotterysystem.common.constants.ControllerErrorConstants;
import com.julien.lotterysystem.entity.errorcode.ErrorCode;
import com.julien.lotterysystem.entity.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
// @ResponseBody、@ControllerAdvice 合并成 @RestControllerAdvice
@RestControllerAdvice
public class ExceptionAdvice {

    /**
     * 处理请求体为空异常
     */
    @ExceptionHandler
    public Object handler(HttpMessageNotReadableException e) {
        log.error("发生异常:{}", e.getMessage());
        return Result.fail(HttpStatus.BAD_REQUEST.value(),"发生异常");
    }

    /**
     * 处理 @Valid 参数校验失败异常
     * 提取 @NotBlank / @Size 等注解中定义的 message 返回给前端
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public Object handler(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        // 取第一个校验失败的字段错误信息
        FieldError fieldError = bindingResult.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "传参校验失败";
        log.warn("传参校验失败: {}", message);
        return Result.fail(new ErrorCode(ControllerErrorConstants.VALIDATION_FAILED,message));
    }

    /**
     * 处理注册校验失败异常
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public Object handler(IllegalArgumentException e) {
        log.error("注册校验失败:{}", e.getMessage());
        return Result.fail(HttpStatus.BAD_REQUEST.value(),"注册校验失败");
    }
}
