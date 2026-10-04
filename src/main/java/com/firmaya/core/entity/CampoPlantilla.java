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
@Table(name = "campo_plantilla")
public class CampoPlantilla {

    // Los marcadores {{campo}} no tienen tipo definido en el documento: se guardan como texto
    public static final String TIPO_TEXTO = "texto";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campo")
    private Integer idCampo;

    @ManyToOne
    @JoinColumn(name = "id_plantilla")
    private Plantilla plantilla;

    @Column(name = "marcador", length = 100)
    private String marcador;

    @Column(name = "tipo_dato", length = 50)
    private String tipoDato;

    public Integer getIdCampo() {
        return idCampo;
    }

    public void setIdCampo(Integer idCampo) {
        this.idCampo = idCampo;
    }

    public Plantilla getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(Plantilla plantilla) {
        this.plantilla = plantilla;
    }

    public String getMarcador() {
        return marcador;
    }

    public void setMarcador(String marcador) {
        this.marcador = marcador;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }
}
