package com.example.demo.common.config.mvc.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestTimeConsumingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestTimeConsumingInterceptor.class);
    private ThreadLocal<StopWatch> stopWatchThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        stopWatchThreadLocal.set(stopWatch);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        String servletPath = httpServletRequest.getServletPath();
        String userIdStr = httpServletRequest.getParameter("userId");
        userIdStr = userIdStr == null ? "null" : userIdStr;
        for (int i = 0, len = userIdStr.length(); i < 8 - len; i++) {
            userIdStr += " ";
        }
        for (int i = 0, len = servletPath.length(); i < 60 - len; i++) {
            servletPath += " ";
        }
        StopWatch stopWatch = stopWatchThreadLocal.get();
        stopWatch.stop();
        logger.info("{}{}{}ms", userIdStr, servletPath, stopWatch.getTotalTimeMillis());
    }
}
