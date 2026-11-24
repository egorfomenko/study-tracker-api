package com.example.taskManager.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form ->form
                                .usernameParameter("email")
                                .successHandler(((request, response, authentication) ->
                                        response.setStatus(HttpServletResponse.SC_NO_CONTENT)))
                                .failureHandler(((request, response, exception) ->
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                                .permitAll()

                        )
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(((request, response, authException) ->
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                        )
                .logout(logout -> logout
                        .logoutSuccessHandler(((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)))
                )
                .build();
    }
}
