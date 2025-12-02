package com.app.GoldenFeets.Config;

import com.app.GoldenFeets.Entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getEmail(), usuario.getPassword(), authorities);
        this.usuario = usuario;
    }

    // ESCRIBIMOS EL GETTER MANUALMENTE PARA EVITAR ERRORES DE LOMBOK
    public Usuario getUsuario() {
        return this.usuario;
    }
}