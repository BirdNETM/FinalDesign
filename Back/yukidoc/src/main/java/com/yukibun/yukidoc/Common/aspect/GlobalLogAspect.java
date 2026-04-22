package com.yukibun.yukidoc.Common.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class GlobalLogAspect {

    // 拦截你自己项目的所有方法
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void allMethods() {}

    @Around("allMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // 方法信息
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("➡️ 调用 {}.{}，参数: {}", className, methodName, args);

        try {
            Object result = joinPoint.proceed();

            log.info("⬅️ 返回 {}.{}，结果: {}，耗时 {} ms",
                    className, methodName, result,
                    System.currentTimeMillis() - start);

            return result;
        } catch (Throwable e) {
            log.error("❌ 异常 {}.{}，耗时 {} ms",
                    className, methodName,
                    System.currentTimeMillis() - start, e);
            throw e;
        }
    }
}