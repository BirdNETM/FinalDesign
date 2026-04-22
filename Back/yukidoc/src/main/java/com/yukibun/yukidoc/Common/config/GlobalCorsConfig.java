package com.yukibun.yukidoc.Common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 1. 允许所有源（开发环境），生产环境替换为具体前端域名
        config.addAllowedOriginPattern("*");
        // 2. 允许携带Cookie（关键：缺失会导致部分场景跨域失败）
        config.setAllowCredentials(true);
        // 3. 允许所有请求方法（包含OPTIONS预检请求）
        config.addAllowedMethod("*");
        // 4. 允许所有请求头
        config.addAllowedHeader("*");
        // 5. 暴露所有响应头（关键：解决浏览器拦截200响应的问题）
        config.addExposedHeader("*");
        // 6. 预检请求缓存时间（减少OPTIONS请求）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有接口生效
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
