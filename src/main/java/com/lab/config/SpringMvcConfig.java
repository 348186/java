package com.lab.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.lab.interceptor.LoginInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * SpringMVC 子容器：Controller、统一异常出口、拦截器、AOP 与消息转换。
 */
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
        "com.lab.controller", "com.lab.common", "com.lab.aspect", "com.lab.interceptor"
})
public class SpringMvcConfig implements WebMvcConfigurer {

    /** 放行默认 Servlet，保证静态资源可访问 */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /** 允许前后端分离场景下的跨域访问 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /** 除登录与测试接口外，其余路径都需要登录 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/logout", "/error", "/favicon.ico", "/test/**");
    }

    /** 修正 String 消息转换器默认 ISO-8859-1 造成的中文乱码 */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter stringConverter) {
                stringConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.getObjectMapper()
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
            }
        }
    }
}
