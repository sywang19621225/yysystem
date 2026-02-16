package com.yysystem.common.aspect;

import com.yysystem.modules.system.service.SysLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysLogService sysLogService;

    @Pointcut("execution(* com.yysystem.modules..controller.*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        
        // 记录操作日志
        recordLog(joinPoint, endTime - startTime);
        
        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long time) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String requestUrl = request.getRequestURL().toString();
            String requestMethod = request.getMethod();
            
            // 获取操作人信息
            String operator = "未知用户";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                operator = authentication.getName();
            }
            
            // 构建操作内容
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = method.getName();
            String operation = className + "." + methodName;
            
            String content = String.format("执行方法: %s, 耗时: %dms", operation, time);
            
            // 异步记录日志
            sysLogService.log("系统操作", content, operator);
            
        } catch (Exception e) {
            // 日志记录失败不应影响主要业务
            e.printStackTrace();
        }
    }
}