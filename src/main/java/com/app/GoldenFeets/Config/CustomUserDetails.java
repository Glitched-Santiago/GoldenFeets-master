package com.app.GoldenFeets.Config;

import com.app.GoldenFeets.Entity.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter // Lombok generará getUsuario() automáticamente
public class CustomUserDetails extends User {

    private final Usuario usuario; // Esta es tu entidad personalizada

    public CustomUserDetails(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        // Pasamos datos básicos al padre (Spring Security User)
        super(usuario.getEmail(), usuario.getPassword(), authorities);
        this.usuario = usuario;
    }
}