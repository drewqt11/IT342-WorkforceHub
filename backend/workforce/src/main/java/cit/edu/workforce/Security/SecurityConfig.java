package cit.edu.workforce.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsServiceImpl userDetailsService,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh-token").permitAll()
                        .requestMatchers("/api/auth/oauth2/token-info/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/public/domains/**").permitAll()

                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // HR endpoints
                        .requestMatchers("/api/hr/**").hasAnyRole("ADMIN", "HR")

                        // Employee endpoints - using method security for fine-grained control
                        .requestMatchers(HttpMethod.GET, "/api/employee/**").hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/employee/profile")
                        .hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // Dashboard endpoints
                        .requestMatchers("/api/auth/dashboard/admin").hasRole("ADMIN")
                        .requestMatchers("/api/auth/dashboard/employee").hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                        // Other endpoints need authentication, with method-level security for
                        // fine-grained control
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
                Arrays.asList("http://localhost:*", "http://127.0.0.1:*", "https://*.workforcehub.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}