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

        // ✅ Allow your production and local origins
        configuration.setAllowedOrigins(List.of(
                "https://investo-frontend.netlify.app",
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        // ✅ Allow all methods (so Vue can make any request) this is main thig
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Allow all headers (especially Authorization)
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Allow credentials (Bearer tokens, etc.)
        configuration.setAllowCredentials(true);

        // ✅ Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // ❌ disable default basic authentication popup
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
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
