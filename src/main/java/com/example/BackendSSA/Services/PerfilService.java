package com.example.BackendSSA.Services;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.BackendSSA.Dtos.DtoPerfil;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional; 

@Service
public class PerfilService {

    private final UserRepository userRepository;

    @Autowired
    public PerfilService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Obtiene los datos personales de un usuario y los mapea a DtoPerfil.
     * @param idUsuario ID del usuario.
     * @return DtoPerfil con los datos del usuario.
     */
    @Transactional(readOnly = true)
    public DtoPerfil obtenerPerfil(Integer idUsuario) {
        Optional<Usuario> usuarioOpt = userRepository.findById(idUsuario);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + idUsuario);
        }
        Usuario usuario = usuarioOpt.get();

        DtoPerfil dto = new DtoPerfil();
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());

        dto.setDireccionCalle(usuario.getDireccionCalle());
        dto.setDireccionCiudad(usuario.getDireccionCiudad());
        dto.setDireccionEstado(usuario.getDireccionEstado());
        
        // Mapeo directo de LocalDate de la entidad a LocalDateTime del DTO
        // Aunque solo almacenamos la fecha, usamos LocalDateTime en el DTO por flexibilidad.
        // CORRECCIÓN 1: Ambos son LocalDateTime, asignación directa.
        dto.setFechaNacimiento(usuario.getFechaNacimiento());
        
         // CORRECCIÓN 2: Convertir el objeto 'Rol' de la entidad a String para el DTO.
        if (usuario.getRol() != null) {
             dto.setRol(usuario.getRol().getNombreRol()); // Asumiendo que Rol es un Enum con .name()
        } else {
             dto.setRol("NO ASIGNADO");
        }
        return dto;
    }

    /**
     * Actualiza los datos personales de un usuario a partir de un DtoPerfil.
     * @param idUsuario ID del usuario a actualizar.
     * @param dtoPerfil DTO con los nuevos datos.
     * @return El DtoPerfil actualizado.
     */
    @Transactional
    public DtoPerfil actualizarPerfil(Integer idUsuario, DtoPerfil dtoPerfil) {
        Optional<Usuario> usuarioOpt = userRepository.findById(idUsuario);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado para actualizar con ID: " + idUsuario);
        }

        Usuario usuario = usuarioOpt.get();
        
        // 1. Actualizar los campos del usuario desde el DTO
        usuario.setNombres(dtoPerfil.getNombres());
        usuario.setApellidos(dtoPerfil.getApellidos());
        usuario.setEmail(dtoPerfil.getEmail()); 
        usuario.setTelefono(dtoPerfil.getTelefono());
        
        // CORRECCIÓN 3: La entidad Usuario usa LocalDateTime, 
        // por lo que asignamos el LocalDateTime del DTO directamente.
        usuario.setFechaNacimiento(dtoPerfil.getFechaNacimiento());

        usuario.setDireccionCalle(dtoPerfil.getDireccionCalle());
        usuario.setDireccionCiudad(dtoPerfil.getDireccionCiudad());
        usuario.setDireccionEstado(dtoPerfil.getDireccionEstado());
        
        // NOTA: El Rol y la Contraseña no se modifican desde este DTO,
        // y 'profesion' se maneja en el servicio de preferencias.
        
        // 2. Guardar los cambios
        userRepository.save(usuario);

        // 3. Retornar el DTO actualizado
        return obtenerPerfil(idUsuario);
    }
    
}
