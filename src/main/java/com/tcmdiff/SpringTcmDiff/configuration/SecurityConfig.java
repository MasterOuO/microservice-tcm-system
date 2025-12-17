package com.tcmdiff.SpringTcmDiff.configuration; 

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 關閉 CSRF 跟設定CORS
        	.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // authorizeHttpRequests() → 開始設定 URL 授權規則 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/diff/**", "/api/**","/save-diff", "/load-diff").permitAll() 
                .requestMatchers("/graph/**").permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }

    // 定義 CORS 規則的 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允許來自 User Service (8090) 的請求
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8090")); 
        
        // 允許的方法 (GET, POST, etc.)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允許的標頭
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 是否允許攜帶憑證 (Cookies 等)，如果有用到 Session 需設為 true
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 套用到所有路徑
        return source;
    }
}