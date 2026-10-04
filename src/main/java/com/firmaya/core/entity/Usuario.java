package com.firmaya.core.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_INACTIVO = "Inactivo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "apellido", length = 100)
    private String apellido;

    // Longitud 254 según CU-15/19/21 (ampliación I8)
    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "contrasenia_hash", length = 255)
    private String contraseniaHash;

    @Column(name = "estado", length = 30)
    private String estado;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    // Fin del bloqueo temporal de 15 minutos (CU-19, ampliación I7)
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContraseniaHash() {
        return contraseniaHash;
    }

    public void setContraseniaHash(String contraseniaHash) {
        this.contraseniaHash = contraseniaHash;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
