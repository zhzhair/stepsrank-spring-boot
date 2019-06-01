package com.example.demo.common.config.aspect.log;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.config.aspect.log.annotation.LogForTask;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
public class LogAspect {

    @Pointcut(value = "execution(* com.example.demo.*.controller.*.*(..))")
    public void logPointCut() {

    }

    @Pointcut(value = "execution(* com.example.demo.task.*.*(..))")
    public void logPointCutOfTask() {

    }

    /**
     * 记录controller日志环绕通知
     */
    @Around(value = "logPointCut() && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object autoLogRecord(ProceedingJoinPoint pjp) throws Throwable {
//        获取request
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes ra = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = ra.getRequest();
//        http请求的方法
        String method = request.getMethod();
//        请求路径
        String servletPath = request.getServletPath();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
//        获取参数名称列表
        String[] parameterNames = signature.getParameterNames();
//        获取方法名
        String name = signature.getName();
//        切入的类
        Class declaringType = signature.getDeclaringType();
//        日志对象
        Logger logger = LoggerFactory.getLogger(declaringType);
//        获取参数列表
        Object[] args = pjp.getArgs();
        Object[] newArr = new Object[args.length + 2];
        StringBuilder var1 = new StringBuilder("前端调用方法开始----" + name + "---->：#{\"URL地址\":{}, \"HTTP方法\":{}，参数：");
        if (args.length != 0) {
            System.arraycopy(args, 0, newArr, 2, args.length);
            for (String s : parameterNames) {
                var1.append(", \"").append(s).append("\":{}");
            }
        }
        var1.append("}");
        newArr[0] = servletPath;
        newArr[1] = method;
//          记录日志
        logger.info(var1.toString(), newArr);
        Object proceed = pjp.proceed();
        logger.info("前端调用方法结束----" + name + "---->：返回值: {}", JSONObject.toJSONString(proceed));
        return proceed;
    }

    /**
     * 记录定时任务日志环绕通知,注解取名叫TaskLog会报错找不到包路径
     */
    @Around(value = "logPointCutOfTask() && @annotation(taskLog)", argNames = "pjp,taskLog")
    public Object autoLogRecordOfTask(ProceedingJoinPoint pjp, LogForTask taskLog) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
//        获取方法名
        String name = signature.getName();
//        切入的类
        Class declaringType = signature.getDeclaringType();
//        日志对象
        Logger logger = LoggerFactory.getLogger(declaringType);
        StopWatch sw = new StopWatch();
        sw.start(name);
        Object proceed = pjp.proceed();
        sw.stop();
        long millis = sw.getTotalTimeMillis();
        if (millis > 1000)//只打印耗时超过1秒的定时任务的日志
//        logger.info("定时任务---"+name+"---结束---->，统计信息:{}",sw.prettyPrint());
            logger.info("\n\t定时任务---" + name + "---结束---->，耗时:{}", millis + "毫秒");
        return proceed;
    }

}
