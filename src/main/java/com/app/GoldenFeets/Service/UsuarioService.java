package com.app.GoldenFeets.Service;

import com.app.GoldenFeets.Config.CustomUserDetails;
import com.app.GoldenFeets.DTO.PerfilInfoDTO;
import com.app.GoldenFeets.DTO.PerfilPasswordDTO;
import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import com.app.GoldenFeets.spec.UsuarioSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioSpecification usuarioSpecification;

    /**
     * Carga un usuario por su email para Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con el email: " + email));

        String role = (usuario instanceof Administrador) ? "ADMIN" : "CLIENTE";
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

        return new CustomUserDetails(usuario, authorities);
    }

    /**
     * Busca un usuario por email y confirma que es un Cliente.
     */
    public Cliente findClienteByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró un usuario con el email: " + email));
        if (!(usuario instanceof Cliente)) {
            throw new IllegalStateException("El usuario encontrado no es un cliente.");
        }
        return (Cliente) usuario;
    }

    // --- MÉTODOS CRUD ---

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> search(String id, String nombres, String apellidos, String numeroDocumento, String email, String rol) {
        Specification<Usuario> spec = usuarioSpecification.findByCriteria(id, nombres, apellidos, numeroDocumento, email, rol);
        return usuarioRepository.findAll(spec);
    }

    /**
     * Guarda un usuario nuevo o actualiza uno existente (Incluyendo Foto y Password).
     */
    @Transactional
    public void saveOrUpdate(Usuario usuarioDataFromForm, String rol) {
        // --- CASO 1: ACTUALIZACIÓN (Tiene ID) ---
        if (usuarioDataFromForm.getId() != null) {
            Usuario usuarioExistente = usuarioRepository.findById(usuarioDataFromForm.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado para actualizar"));

            // Actualizamos campos básicos
            usuarioExistente.setNombres(usuarioDataFromForm.getNombres());
            usuarioExistente.setApellidos(usuarioDataFromForm.getApellidos());
            usuarioExistente.setEmail(usuarioDataFromForm.getEmail());
            usuarioExistente.setNumeroDocumento(usuarioDataFromForm.getNumeroDocumento());
            usuarioExistente.setFechaNacimiento(usuarioDataFromForm.getFechaNacimiento());

            // ACTUALIZACIÓN DE FOTO: Solo si viene una nueva (no nula y no vacía)
            if (usuarioDataFromForm.getFotoUrl() != null && !usuarioDataFromForm.getFotoUrl().isEmpty()) {
                usuarioExistente.setFotoUrl(usuarioDataFromForm.getFotoUrl());
            }

            // ACTUALIZACIÓN DE PASSWORD: Solo si el usuario escribió una nueva
            if (usuarioDataFromForm.getPassword() != null && !usuarioDataFromForm.getPassword().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioDataFromForm.getPassword()));
            }

            usuarioRepository.save(usuarioExistente);

        } else {
            // --- CASO 2: CREACIÓN (Nuevo Usuario) ---
            Usuario nuevoUsuario;

            // Determinamos la instancia según el rol seleccionado
            if ("ADMIN".equals(rol)) {
                nuevoUsuario = new Administrador();
            } else {
                nuevoUsuario = new Cliente();
            }

            // Llenamos datos
            nuevoUsuario.setNombres(usuarioDataFromForm.getNombres());
            nuevoUsuario.setApellidos(usuarioDataFromForm.getApellidos());
            nuevoUsuario.setEmail(usuarioDataFromForm.getEmail());
            nuevoUsuario.setNumeroDocumento(usuarioDataFromForm.getNumeroDocumento());
            nuevoUsuario.setFechaNacimiento(usuarioDataFromForm.getFechaNacimiento());

            // Asignamos la foto inicial (puede ser null, el front pondrá placeholder)
            nuevoUsuario.setFotoUrl(usuarioDataFromForm.getFotoUrl());

            // Encriptamos la contraseña obligatoria
            nuevoUsuario.setPassword(passwordEncoder.encode(usuarioDataFromForm.getPassword()));

            usuarioRepository.save(nuevoUsuario);
        }
    }

    // --- MÉTODOS PARA "MI PERFIL" ---

    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
    }

    @Transactional
    public void actualizarInfoPerfil(String username, PerfilInfoDTO infoDTO) {
        Usuario usuario = buscarPorUsername(username);

        // Validar si el nuevo email ya existe en OTRO usuario
        if (!infoDTO.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.findByEmail(infoDTO.getEmail()).isPresent()) {
                throw new RuntimeException("El email ya está en uso.");
            }
            usuario.setEmail(infoDTO.getEmail());
        }

        usuario.setNombres(infoDTO.getNombres());
        usuario.setApellidos(infoDTO.getApellidos());

        // Si viene foto nueva, la actualizamos
        if (infoDTO.getFotoUrl() != null && !infoDTO.getFotoUrl().isEmpty()) {
            usuario.setFotoUrl(infoDTO.getFotoUrl());
        }

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(String username, PerfilPasswordDTO passwordDTO) {
        if (!passwordDTO.getNuevaPassword().equals(passwordDTO.getConfirmarPassword())) {
            throw new RuntimeException("Las nuevas contraseñas no coinciden.");
        }

        Usuario usuario = buscarPorUsername(username);

        if (!passwordEncoder.matches(passwordDTO.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta.");
        }

        usuario.setPassword(passwordEncoder.encode(passwordDTO.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }
}