package com.app.GoldenFeets.Config;

import com.app.GoldenFeets.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. Rutas de Administrador (requieren rol ADMIN)
                        .requestMatchers("/admin/**", "/inventario/**", "/usuarios/**").hasRole("ADMIN")

                        // 2. Rutas de Cliente (requieren rol CLIENTE - solo el carrito y la compra)
                        .requestMatchers("/carrito/**", "/pedidos/**").hasRole("CLIENTE")

                        // 3. Rutas Públicas (accesibles para todos, incluyendo el catálogo y sus detalles)
                        .requestMatchers("/", "/catalogo/**", "/login", "/registro", "/Global.css", "/js/**", "/images/**").permitAll()

                        // 4. El resto de peticiones (cualquier otra URL no definida) requieren autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

                            if (isAdmin) {
                                response.sendRedirect("/admin/dashboard");
                            } else {
                                // Redirige al catálogo después de que un cliente inicie sesión
                                response.sendRedirect("/catalogo");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}