package com.zeta_horizon.investment_portfolio_tracker.config;

//import com.zeta_horizon.investment_portfolio_tracker.filters.JWTFilter;
import com.zeta_horizon.investment_portfolio_tracker.filters.JWTFilter;
import com.zeta_horizon.investment_portfolio_tracker.service.implementation.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    private JWTFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests only from your frontend's origin (localhost:5173)
        configuration
                .setAllowedOrigins(List.of("http://localhost:5173",
                        "https://investment-portfolio-tracker-frontend", "http://localhost:3000"));

        // Allow common HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers (you can restrict this to specific headers if needed)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, Authorization headers, etc.) to be included in
        // requests
        configuration.setAllowCredentials(true);

        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF protection (typically okay for stateless REST APIs)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS with default configuration (will use corsConfigurationSource
                // bean)
                .cors(Customizer.withDefaults())

                // Enable HTTP Basic authentication (not often used in production, but okay for
                // testing or simple APIs)
                .httpBasic(Customizer.withDefaults())

                // Configure session management to be stateless (no HTTP session will be created
                // or used)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules:
                .authorizeHttpRequests(auth -> auth

                        // Allow anyone to access /auth/register and /auth/login
                        .requestMatchers("/auth/register", "/auth/login").permitAll()

                        // Only allow users with the "ADMIN" role to access /admin/users
                        .requestMatchers("/admin/users").hasRole("ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated())

                // Add custom JWT filter before the default UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // Build and return the configured SecurityFilterChain
                .build();
    }

    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
    // Exception {
    // return http
    // .csrf(AbstractHttpConfigurer::disable)
    // .httpBasic(Customizer.withDefaults())
    // .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    // .authorizeHttpRequests(auth -> auth
    // .requestMatchers("/auth/register", "/auth/login").permitAll()
    // .requestMatchers("/admin/users").hasRole("ADMIN")
    // .anyRequest().authenticated()
    // )
    // .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
    // .build();
    // }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
