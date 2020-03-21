package com.atguigu.gmall.config;

import com.atguigu.gmall.interceptors.Auinterceptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by 辉 on 2020/3/13.
 * 添加拦截器到springboot
 */
@Configuration //表明这是一个配置类
public class WebMvcConfiguration  extends WebMvcConfigurerAdapter {
    @Autowired
    Auinterceptors  auinterceptors;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //忽略error的请求，不拦截
        registry.addInterceptor(auinterceptors).addPathPatterns("/**").
                excludePathPatterns("/error");
        super.addInterceptors(registry);
    }
}
