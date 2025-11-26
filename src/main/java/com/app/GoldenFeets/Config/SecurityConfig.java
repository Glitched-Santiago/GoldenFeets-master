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
                        // 1. ZONA ADMIN (Protegida)
                        .requestMatchers("/admin/**", "/inventario/**", "/usuarios/**").hasRole("ADMIN")

                        // 2. ZONA CLIENTE / COMPRAS (Requiere Login)
                        // Solo pedimos login cuando va a pagar o ver sus pedidos pasados
                        .requestMatchers("/pedidos/**").authenticated()

                        // 3. ZONA PÚBLICA (Todo esto es accesible sin login)
                        .requestMatchers(
                                "/", "/home", "/index",           // Páginas de inicio
                                "/catalogo/**",                   // Ver productos y detalles
                                "/carrito/**",                    // Ver y añadir al carrito
                                "/login", "/registro",            // Autenticación
                                "/error",                         // Para ver mensajes de error sin redirigir al login
                                "/css/**", "/js/**", "/images/**", "/webjars/**", // Recursos estáticos carpetas
                                "/*.css", "/Global.css", "/Admin.css" // Recursos estáticos raíz
                        ).permitAll()

                        // Cualquier otra ruta no listada arriba requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            // Lógica para redirigir según el rol
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));

                            if (isAdmin) {
                                response.sendRedirect("/admin/dashboard");
                            } else {
                                // Si el usuario estaba intentando comprar, Spring lo llevará allí.
                                // Si no, lo llevamos al catálogo.
                                response.sendRedirect("/catalogo");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL para cerrar sesión (debe ser POST)
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}