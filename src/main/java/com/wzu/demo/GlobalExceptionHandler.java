package com.wzu.demo;

import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常（用户不存在等）
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return new Result<>(e.getCode(), e.getMessage(), null);
    }

    // @RequestParam 上的校验约束（需要 Controller 类加 @Validated）
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        return new Result<>(400, e.getMessage(), null);
    }

    // @RequestBody DTO 的 @Valid 校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "参数错误" : fe.getField() + " " + fe.getDefaultMessage();
        return new Result<>(400, msg, null);
    }

    // 兜底：未捕获异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        return new Result<>(500, "服务器内部错误", null);
    }
}