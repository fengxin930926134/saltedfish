package com.fengx.saltedfish.config.mvc;

import com.fengx.saltedfish.interceptor.HttpInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * spring配置类
 */
@Slf4j
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(5000);
        return factory;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 配置编码
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter)converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //        registry.addMapping("/test/cros")
        //                // -------addMapping后还可以继续配置-------
        //                .allowedOrigins("http://kmind.top/h5/")
        //                .maxAge(300L);
//        registry.addMapping("/**").allowedOrigins("*");
        registry.addMapping("/**").allowedOrigins("*");
    }

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String url = "/**";
        registry.addInterceptor(new HttpInterceptor())
                .addPathPatterns(url)
                // 放行url
                .excludePathPatterns();
    }
}
