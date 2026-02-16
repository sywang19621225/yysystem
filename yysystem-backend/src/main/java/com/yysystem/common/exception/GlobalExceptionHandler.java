package com.yysystem.common.exception;

import com.yysystem.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public Result<String> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("参数校验异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("参数绑定异常: {}", message);
        return Result.error(400, message);
    }

    /**
     * 权限不足
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足，请联系管理员");
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<String> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }

    /**
     * 唯一约束等数据库约束异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("sys_role.uk_role_name")) {
            return Result.error(400, "角色名称已存在");
        }
        return Result.error(400, "数据库约束错误: " + e.getMessage());
    }
}
