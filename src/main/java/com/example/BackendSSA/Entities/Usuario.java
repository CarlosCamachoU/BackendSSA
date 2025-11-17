package com.example.BackendSSA.Entities;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "Usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"preferencias"}) 
@EqualsAndHashCode(callSuper = false, exclude = {"preferencias"})
public class Usuario {
     
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Integer idUsuario;

    // Relación Muchos a Uno (ManyToOne) con la tabla ROL (Columna id_rol)
    @ManyToOne(fetch = FetchType.EAGER) // Carga inmediata del rol
    @JoinColumn(name = "idrol", nullable = false)
    private Rol rol;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // Contraseña cifrada (hash). Es crucial que sea una longitud mayor a la predeterminada.
    @Column(name = "contrasenahash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecharegistro", columnDefinition = "TIMESTAMP", updatable = false)
    private LocalDateTime fechaRegistro;

    // Columna para control de estado (activo/inactivo)
    @Column(name = "esActivo")
    private Boolean esActivo;

    @Column(name = "resetPasswordToken", length = 255)
    private String resetPasswordToken;

    @Column(name = "tokenExpiryDate", columnDefinition = "TIMESTAMP")
    private LocalDateTime tokenExpiryDate;

    // NOTA: Se usan 'NULL' en la DB (por el script SQL) para no afectar usuarios existentes
    @Column(name = "telefono", length = 20)
    private String telefono; 
    
    @Column(name = "fechaNacimiento", columnDefinition = "DATE") 
    private LocalDateTime fechaNacimiento; 
    
    @Column(name = "direccion_calle", length = 255)
    private String direccionCalle;
    
    @Column(name = "direccion_ciudad",  length = 255)
    private String direccionCiudad;
    
    @Column(name = "direccion_estado",  length = 255)
    private String direccionEstado;
    
    // RELACIÓN ONE-TO-ONE CON PREFERENCIAS
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore // Crucial: Evita la serialización circular con la entidad Preferencias
    private PreferenciasEntities preferencias;

    // Constructor sin ID (usado al crear un nuevo usuario)
    public Usuario(String nombres, String apellidos, String email, String contrasenaHash, Rol rol) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.fechaRegistro = LocalDateTime.now();
        this.esActivo = true;
    }  
}
