package com.mzn.Stadium.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pays {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPays;

    private String nomPays;

    private String continent;

    private String codePays;

    @JsonIgnore
    @OneToMany(mappedBy = "pays")
    private List<Stade> stades;

    public Pays() {
    }

    public Pays(String nomPays, String continent, List<Stade> stades) {
        this.nomPays = nomPays;
        this.continent = continent;
        this.stades = stades;
    }

    public Pays(String nomPays, String continent, String codePays, List<Stade> stades) {
        this.nomPays = nomPays;
        this.continent = continent;
        this.codePays = codePays;
        this.stades = stades;
    }

    public Long getIdPays() {
        return idPays;
    }

    public void setIdPays(Long idPays) {
        this.idPays = idPays;
    }

    public String getNomPays() {
        return nomPays;
    }

    public void setNomPays(String nomPays) {
        this.nomPays = nomPays;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCodePays() {
        return codePays != null ? codePays : continent;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

    public List<Stade> getStades() {
        return stades;
    }

    public void setStades(List<Stade> stades) {
        this.stades = stades;
    }

    @Override
    public String toString() {
        return "Pays [idPays=" + idPays
                + ", nomPays=" + nomPays
                + ", continent=" + continent
                + ", codePays=" + codePays + "]";
    }
}
