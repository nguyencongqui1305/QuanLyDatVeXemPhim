package com.qlrapphim.config;

import com.qlrapphim.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/home",
                    "/phim/**",
                    "/tim-kiem",
                    "/login", "/register",
                    "/css/**", "/js/**", "/images/**", "/webjars/**",
                    "/api/lich-chieu/**",
                    "/403", "/error"
                ).permitAll()
                .requestMatchers("/dat-ve/**", "/thanh-toan/**", "/lich-su/**", "/tai-khoan/**")
                    .hasAnyRole("KHACH_HANG", "NHAN_VIEN", "QUAN_LY")
                .requestMatchers("/admin/phim/**", "/admin/lich-chieu/**",
                                 "/admin/bao-cao/**", "/admin/khuyen-mai/**")
                    .hasRole("QUAN_LY")
                .requestMatchers("/admin/**")
                    .hasAnyRole("NHAN_VIEN", "QUAN_LY")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/dat-ve/giu-ghe")
            );

        return http.build();
    }
}
