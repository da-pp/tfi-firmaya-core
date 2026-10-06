package com.firmaya.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "estado_contrato")
public class EstadoContrato {

    public static final String BORRADOR = "Borrador";
    public static final String EN_REVISION = "En Revisión";
    public static final String LISTO_PARA_FIRMAR = "Listo para firmar";
    public static final String FIRMADO = "Firmado";
    public static final String ARCHIVADO = "Archivado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer idEstado;

    @Column(name = "nombre", length = 50)
    private String nombre;

    public Integer getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(Integer idEstado) {
        this.idEstado = idEstado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
