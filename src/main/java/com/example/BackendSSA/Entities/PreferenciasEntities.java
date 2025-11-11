package com.example.BackendSSA.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table(name = "preferencias")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"usuario"}) 
@EqualsAndHashCode(callSuper = false, exclude = {"usuario"})
public class PreferenciasEntities {

    // El ID de la preferencia es el mismo ID del usuario (clave primaria y foránea)
    @Id
    //@Column(name = "idusuario")
    private Integer idUsuario; 

    // Relación OneToOne con Usuario. 
    // MapsId indica que el valor de la clave primaria (idUsuario) se obtiene de la clave foránea.
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId 
    @JoinColumn(name = "idusuario", insertable = false, updatable = false)
    private Usuario usuario; 

    // CAMPOS DE PERSONALIZACIÓN (Almacenados como CSV String)
    
    @Column(name = "hobbies", length = 500)
    private String hobbies; 

    @Column(name = "coloresFavoritos", length = 255)
    private String coloresFavoritos; 

    @Column(name = "intereses", length = 500)
    private String intereses; 

    @Column(name = "tallas", length = 50)
    private String tallas; 
    
    @Column (name = "Profesion", length = 500)
    private String profesion;

    @Column(name = "estilos", length = 255)
    private String estilos;
    
}
