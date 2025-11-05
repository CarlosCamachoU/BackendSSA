package com.example.BackendSSA.Entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "Usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
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






}
