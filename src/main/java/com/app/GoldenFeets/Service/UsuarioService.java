package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService { // <-- IMPLEMENTA LA INTERFAZ

    private final UsuarioRepository usuarioRepository;

    /**
     * Este método es requerido por Spring Security.
     * Carga un usuario por su email y lo convierte en un objeto UserDetails
     * que Spring Security puede entender.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con el email: " + email));

        // Obtenemos el rol del usuario para asignarlo como una "autoridad"
        // Es una convención de Spring Security anteponer "ROLE_" al nombre del rol.
        String role = "";
        if (usuario instanceof Administrador) {
            role = "ADMIN";
        } else if (usuario instanceof Cliente) {
            role = "CLIENTE";
        }

        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

        // Creamos y retornamos un objeto UserDetails con los datos del usuario
        return new User(usuario.getEmail(), usuario.getPassword(), authorities);
    }

    /**
     * Busca un usuario por su email y verifica que sea una instancia de Cliente.
     */
    public Cliente findClienteByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró un usuario con el email: " + email));

        if (!(usuario instanceof Cliente)) {
            throw new IllegalStateException("El usuario encontrado no es un cliente.");
        }
        return (Cliente) usuario;
    }
}