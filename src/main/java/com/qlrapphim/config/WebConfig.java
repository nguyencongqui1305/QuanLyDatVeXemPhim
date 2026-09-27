package com.qlrapphim.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("vi", "VN"));
        return resolver;
    }

    /**
     * Force UTF-8 encoding cho mọi request và response.
     * QUAN TRỌNG: Dùng FilterRegistrationBean với Ordered.HIGHEST_PRECEDENCE
     * để filter này chạy TRƯỚC Spring Security filter.
     * Nếu Security filter đọc body trước, tiếng Việt trong POST form sẽ bị Mojibake.
     */
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilterRegistration() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceRequestEncoding(true);
        filter.setForceResponseEncoding(true);

        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response,
                                   Object handler, ModelAndView modelAndView) {
                if (modelAndView != null) {
                    modelAndView.addObject("currentURI", request.getRequestURI());
                }
            }
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        // Serve ảnh upload từ filesystem (ngoài classpath) để không cần restart
        Path posterPath = Paths.get(uploadDir, "images", "posters").toAbsolutePath();
        registry.addResourceHandler("/images/posters/**")
                .addResourceLocations("file:" + posterPath.toString().replace("\\", "/") + "/");
    }
}
