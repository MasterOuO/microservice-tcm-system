package com.tcmuser.SpringTcmUser.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.tcmuser.SpringTcmUser.service.CustomSuccessHandler;
import com.tcmuser.SpringTcmUser.service.CustomUserDetailsService;

//默認配置文件
@Configuration
//開啟spring sec自定義配置
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	CustomSuccessHandler customSuccessHandler;
	
	@Autowired
	CustomUserDetailsService customUserDetailsService;
	
	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	//HttpSecurity是Spring Security的主要配置物件 設定請求安全策略
	@Bean 
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		//暫時關閉 太搞事
		http
			.csrf(c -> c.disable()) 
		
			//authorizeHttpRequests() → 開始設定 URL 授權規則
			.authorizeHttpRequests( request -> request
					.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
					.requestMatchers("/appointment").permitAll()	//門診系統預約看診 任何人都可以進入				
					.requestMatchers("/test.html", "/static/**", "/api-test/**", "/ui-test/**", "/diffgraph/**").permitAll()
					.requestMatchers("/api/*/dates").permitAll()
					.requestMatchers("/admin-page").hasAnyAuthority("admin","topadmin") //辨證系統要權限
					.requestMatchers("/doctor-page").hasAuthority("doctor")
					//.anyRequest().permitAll())
					.anyRequest().authenticated()
					)
					
												  
												  
			//.loginPage: controller看要執行哪個操做
			//.loginProcessingUrl: 成功處理完登入(登出)動作會導向的網址
			//.successHandler: 容許自訂成功登入(登出)後的動作或重導向，若有此function則會忽略processingUrl									   
			.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
						.successHandler(customSuccessHandler).permitAll())

						 
			.logout(form -> form.invalidateHttpSession(true).clearAuthentication(true)
						.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.logoutSuccessUrl("/login?logout").permitAll());
		
		/*http
        // 1. 關閉 CSRF 防護（否則你的 POST 請求會被擋掉）
        .csrf(csrf -> csrf.disable()) 
        // 2. 允許所有請求，不做任何檢查
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() 
        )
        // 3. 關閉預設的登入頁面（避免它一直跳轉到 /login）
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable());*/

		return http.build();
		
	}
	
	//AuthenticationManagerBuilder : 用來配置全局的驗證資訊，AuthenticationProvider 和 UserDetailsService 決定怎麼驗證使用者身份
	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
	}
	
}
