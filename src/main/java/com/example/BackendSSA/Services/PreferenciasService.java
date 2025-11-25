package com.example.BackendSSA.Services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.BackendSSA.Dtos.DtoPreferencias;
import com.example.BackendSSA.Entities.PreferenciasEntities;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.PreferenciasRepository;
import com.example.BackendSSA.Repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional; 


@Service
public class PreferenciasService {

    private final PreferenciasRepository preferenciasRepository;
    private final UserRepository userRepository;
    //private final PreferenciasWriterServices preferenciasWriterServices; // Inyectado
    
    @Autowired
    public PreferenciasService(
        PreferenciasRepository preferenciasRepository, 
        UserRepository userRepository
        /*PreferenciasWriterServices preferenciasWriterServices*/) {
        
        this.preferenciasRepository = preferenciasRepository;
        this.userRepository = userRepository;
        //this.preferenciasWriterServices = preferenciasWriterServices;
    }

    // --- MÉTODOS DE MAPEO INTERNO ---
    /** Mapea Entidad a DTO */
    private DtoPreferencias mapearADto(PreferenciasEntities entity) {
        DtoPreferencias dto = new DtoPreferencias();
        dto.setHobbies(entity.getHobbies());
        dto.setColoresFavoritos(entity.getColoresFavoritos());
        dto.setIntereses(entity.getIntereses());
        dto.setTallas(entity.getTallas());
        dto.setEstilos(entity.getEstilos());
        dto.setProfesion(entity.getProfesion());
        return dto;
    }

    /** Mapea DTO a Entidad */
    private PreferenciasEntities mapearAEntidad(DtoPreferencias dto, PreferenciasEntities entity) {
        entity.setHobbies(dto.getHobbies());
        entity.setColoresFavoritos(dto.getColoresFavoritos());
        entity.setIntereses(dto.getIntereses());
        entity.setTallas(dto.getTallas());
        entity.setEstilos(dto.getEstilos());
        entity.setProfesion(dto.getProfesion());
        return entity;
    }

    // --- OPERACIONES CRUD ---

    /**
     * Obtiene las preferencias. Si no existen, devuelve un DTO vacío.
     */
    @Transactional(readOnly = true)
    public DtoPreferencias obtenerPreferencias(Integer idUsuario) {
        Optional<PreferenciasEntities> preferenciasOpt = preferenciasRepository.findById(idUsuario);

        if (preferenciasOpt.isEmpty()) {
            return new DtoPreferencias();
        }

        return mapearADto(preferenciasOpt.get());
    }
    
     /**
     * Actualiza o crea las preferencias del usuario.
     */
    @Transactional
    public DtoPreferencias actualizarPreferencias(Integer idUsuario, DtoPreferencias dto) {
        
        // 1. Verificar si el usuario existe 
        Usuario usuario = userRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado para actualizar preferencias con ID: " + idUsuario));
        
        // 2. Buscar la entidad existente
        Optional<PreferenciasEntities> preferenciasOpt = preferenciasRepository.findById(idUsuario);

        PreferenciasEntities preferencias;
        
        if (preferenciasOpt.isEmpty()) {
            // 3. Si NO existe: CREAR e INSERTAR inmediatamente
            
            PreferenciasEntities newPref = new PreferenciasEntities();
            // IMPORTANTE: Solo establecer la relación con el objeto principal (Usuario)
            // @MapsId se encarga de usar el ID de 'usuario' como PK de 'newPref'.
            newPref.setUsuario(usuario); 
            // newPref.setIdUsuario(idUsuario); <--- Eliminamos esta línea manual
            
            newPref = mapearAEntidad(dto, newPref);

            // Guardamos y HACEMOS FLUSH para forzar el INSERT en la DB
            // Si esto no funciona, el error está en el driver o en el mapeo de la columna en BD.
            preferencias = preferenciasRepository.saveAndFlush(newPref);
            
        } else {
            // 4. Si YA existe: Actualizar la entidad encontrada
            preferencias = preferenciasOpt.get();
            preferencias = mapearAEntidad(dto, preferencias);
            
            // 5. Persistir (UPDATE)
            preferencias = preferenciasRepository.save(preferencias);
        }

        usuario.setPerfilCompleto(true);
        userRepository.save(usuario);
        
        return mapearADto(preferencias);
    }
   
}
