package com.firmaya.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transicion_estado")
public class TransicionEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transicion")
    private Integer idTransicion;

    @ManyToOne
    @JoinColumn(name = "id_estado_origen")
    private EstadoContrato estadoOrigen;

    @ManyToOne
    @JoinColumn(name = "id_estado_destino")
    private EstadoContrato estadoDestino;

    public Integer getIdTransicion() {
        return idTransicion;
    }

    public void setIdTransicion(Integer idTransicion) {
        this.idTransicion = idTransicion;
    }

    public EstadoContrato getEstadoOrigen() {
        return estadoOrigen;
    }

    public void setEstadoOrigen(EstadoContrato estadoOrigen) {
        this.estadoOrigen = estadoOrigen;
    }

    public EstadoContrato getEstadoDestino() {
        return estadoDestino;
    }

    public void setEstadoDestino(EstadoContrato estadoDestino) {
        this.estadoDestino = estadoDestino;
    }
}
