package com.example.BackendSSA.Services;
/*
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.BackendSSA.Entities.PreferenciasEntities;
import com.example.BackendSSA.Entities.Usuario;
import com.example.BackendSSA.Repositories.PreferenciasRepository;*/

//@Service
public class PreferenciasWriterServices {
    /*private final PreferenciasRepository preferenciasRepository;

    @Autowired
    public PreferenciasWriterServices(PreferenciasRepository preferenciasRepository) {
        this.preferenciasRepository = preferenciasRepository;
    }

    /**
     * Crea e inserta la entidad Preferencias inicial en una TRANSACCIÓN NUEVA.
     * El REQUIRES_NEW garantiza que el INSERT inicial se complete antes de que la
     * transacción del PUT/UPDATE continúe, resolviendo el error de @MapsId.
     
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PreferenciasEntities inicializarPreferencias(Usuario usuario) {
        PreferenciasEntities preferencias = new PreferenciasEntities();
        
        // Se establecen los IDs y la relación
        //preferencias.setIdUsuario(usuario.getIdUsuario());
        preferencias.setUsuario(usuario);

        // Se inicializan los campos (o se dejan null, según el DTO)
        preferencias.setHobbies(null);
        preferencias.setColoresFavoritos(null);
        preferencias.setIntereses(null);
        preferencias.setTallas(null);
        preferencias.setEstilos(null);
        preferencias.setProfesion(null);

        // Persistir y hacer FLUSH inmediato
        return preferenciasRepository.saveAndFlush(preferencias);
    }
    */
}
