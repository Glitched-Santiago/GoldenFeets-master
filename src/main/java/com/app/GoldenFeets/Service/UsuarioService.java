package com.app.GoldenFeets.Service;

// --- Imports Añadidos ---
import com.app.GoldenFeets.Config.CustomUserDetails;
import com.app.GoldenFeets.DTO.PerfilInfoDTO;
import com.app.GoldenFeets.DTO.PerfilPasswordDTO;
import org.springframework.transaction.annotation.Transactional;
// --- Fin Imports Añadidos ---

import com.app.GoldenFeets.Entity.Administrador;
import com.app.GoldenFeets.Entity.Cliente;
import com.app.GoldenFeets.Entity.Usuario;
import com.app.GoldenFeets.Repository.UsuarioRepository;
import com.app.GoldenFeets.spec.UsuarioSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
     * Método requerido por Spring Security para cargar un usuario por su email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con el email: " + email));

        String role = (usuario instanceof Administrador) ? "ADMIN" : "CLIENTE";
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

        // --- CAMBIO AQUÍ ---
        // Antes tenías: return new User(...)
        // Ahora usa tu clase personalizada:
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

    /**
     * Devuelve una lista de todos los usuarios en la base de datos.
     */
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     */
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca usuarios por múltiples criterios usando especificaciones.
     */
    public List<Usuario> search(String id, String nombres, String apellidos, String numeroDocumento, String email, String rol) {
        Specification<Usuario> spec = usuarioSpecification.findByCriteria(id, nombres, apellidos, numeroDocumento, email, rol);
        return usuarioRepository.findAll(spec);
    }

    /**
     * Guarda un usuario nuevo o actualiza uno existente.
     */
    public void saveOrUpdate(Usuario usuarioDataFromForm, String rol) {
        // Si el ID no es nulo, es una ACTUALIZACIÓN
        if (usuarioDataFromForm.getId() != null) {
            Usuario usuarioExistente = usuarioRepository.findById(usuarioDataFromForm.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado para actualizar"));

            usuarioExistente.setNombres(usuarioDataFromForm.getNombres());
            usuarioExistente.setApellidos(usuarioDataFromForm.getApellidos());
            usuarioExistente.setEmail(usuarioDataFromForm.getEmail());
            usuarioExistente.setNumeroDocumento(usuarioDataFromForm.getNumeroDocumento());
            usuarioExistente.setFechaNacimiento(usuarioDataFromForm.getFechaNacimiento());

            // Solo actualizamos la contraseña si se proporcionó una nueva en el formulario
            if (usuarioDataFromForm.getPassword() != null && !usuarioDataFromForm.getPassword().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioDataFromForm.getPassword()));
            }

            usuarioRepository.save(usuarioExistente);
        } else { // Si el ID es nulo, es una CREACIÓN
            Usuario nuevoUsuario;
            if ("ADMIN".equals(rol)) {
                nuevoUsuario = new Administrador();
            } else {
                nuevoUsuario = new Cliente();
            }

            nuevoUsuario.setNombres(usuarioDataFromForm.getNombres());
            nuevoUsuario.setApellidos(usuarioDataFromForm.getApellidos());
            nuevoUsuario.setEmail(usuarioDataFromForm.getEmail());
            nuevoUsuario.setNumeroDocumento(usuarioDataFromForm.getNumeroDocumento());
            nuevoUsuario.setFechaNacimiento(usuarioDataFromForm.getFechaNacimiento());
            nuevoUsuario.setPassword(passwordEncoder.encode(usuarioDataFromForm.getPassword()));

            usuarioRepository.save(nuevoUsuario);
        }
    }

    /**
     * Elimina un usuario por su ID.
     */
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    // --- MÉTODOS NUEVOS PARA "MI PERFIL" ---

    /**
     * Busca un usuario por su username (que usamos como el email de login).
     * Usado por el GlobalControllerAdvice y el PerfilController.
     * @param username El email/username del usuario logueado.
     * @return La entidad Usuario.
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        // Usamos el mismo método que tu loadUserByUsername
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
    }

    /**
     * Actualiza la información básica del perfil de un usuario.
     * @param username El username del usuario actual.
     * @param infoDTO El DTO con el nombre, email y fotoUrl.
     */
    @Transactional
    public void actualizarInfoPerfil(String username, PerfilInfoDTO infoDTO) {
        Usuario usuario = buscarPorUsername(username);

        // Opcional: Validar si el nuevo email ya está en uso por OTRO usuario
        if (!infoDTO.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.findByEmail(infoDTO.getEmail()).isPresent()) {
                throw new RuntimeException("El nuevo email ya está en uso por otra cuenta.");
            }
            usuario.setEmail(infoDTO.getEmail());
        }

        // Actualizar los datos desde el DTO
        usuario.setNombres(infoDTO.getNombres());
        usuario.setApellidos(infoDTO.getApellidos());
        usuario.setFotoUrl(infoDTO.getFotoUrl()); // Asume que añadiste 'fotoUrl' a tu entidad Usuario

        // Guardar los cambios
        usuarioRepository.save(usuario);
    }

    /**
     * Cambia la contraseña de un usuario de forma segura.
     * @param username El username del usuario actual.
     * @param passwordDTO El DTO con la contraseña actual y la nueva.
     */
    @Transactional
    public void cambiarPassword(String username, PerfilPasswordDTO passwordDTO) {
        // 1. Validar que las contraseñas nuevas coincidan
        if (!passwordDTO.getNuevaPassword().equals(passwordDTO.getConfirmarPassword())) {
            throw new RuntimeException("Error: Las nuevas contraseñas no coinciden.");
        }

        // 2. Obtener el usuario
        Usuario usuario = buscarPorUsername(username);

        // 3. Validar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordDTO.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("Error: La contraseña actual es incorrecta.");
        }

        // 4. Codificar y guardar la nueva contraseña
        usuario.setPassword(passwordEncoder.encode(passwordDTO.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }
}