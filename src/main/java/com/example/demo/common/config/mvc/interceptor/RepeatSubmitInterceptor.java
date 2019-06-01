package com.example.demo.common.config.mvc.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.dto.BaseResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {
    @Resource(name = "stringRedisTemplate")
    private RedisTemplate redisTemplate;
    private static final RedisScript LUA_SCRIPT = new DefaultRedisScript("local flag = redis.call('get',KEYS[1]);if flag then return false; else redis.call('setex',KEYS[1],10,''); return true; end", Long.class);

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String userIdStr = httpServletRequest.getParameter("userId");
        if (userIdStr != null) {
            String key = userIdStr + ":" + httpServletRequest.getServletPath() + ":" + httpServletRequest.getMethod();
            List<String> keys = new ArrayList<>(1);
            keys.add(key);
            Object result = redisTemplate.execute(LUA_SCRIPT, keys);
            if (result == null) {
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setCode(-3);
                baseResponse.setMsg("重复提交");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpServletResponse.getOutputStream());
                outputStreamWriter.write(JSONObject.toJSONString(baseResponse));
                outputStreamWriter.flush();
                outputStreamWriter.close();
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        String userIdStr = httpServletRequest.getParameter("userId");
        if (userIdStr != null) {
            String key = userIdStr + ":" + httpServletRequest.getServletPath() + ":" + httpServletRequest.getMethod();
            redisTemplate.delete(key);
        }
    }
}
