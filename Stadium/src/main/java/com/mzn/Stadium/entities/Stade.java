package com.mzn.Stadium.entities;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStade;

    private String nomStade;

    private String ville;

    private Integer capacite;

    @JsonProperty("dateInauguration")
    @JsonAlias({ "dateInauguration", "dateConstruction" })
    private Date dateConstruction;

    @ManyToOne
    private Pays pays;

    public Stade() {
    }

    public Stade(String nomStade, Integer capacite,
            Date dateConstruction, Pays pays) {
        this.nomStade = nomStade;
        this.capacite = capacite;
        this.dateConstruction = dateConstruction;
        this.pays = pays;
    }

    public Stade(String nomStade, String ville, Integer capacite,
            Date dateConstruction, Pays pays) {
        this.nomStade = nomStade;
        this.ville = ville;
        this.capacite = capacite;
        this.dateConstruction = dateConstruction;
        this.pays = pays;
    }

    public Long getIdStade() {
        return idStade;
    }

    public void setIdStade(Long idStade) {
        this.idStade = idStade;
    }

    public String getNomStade() {
        return nomStade;
    }

    public void setNomStade(String nomStade) {
        this.nomStade = nomStade;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Date getDateConstruction() {
        return dateConstruction;
    }

    public void setDateConstruction(Date dateConstruction) {
        this.dateConstruction = dateConstruction;
    }

    public Pays getPays() {
        return pays;
    }

    public void setPays(Pays pays) {
        this.pays = pays;
    }

    @Override
    public String toString() {
        return "Stade [idStade=" + idStade
                + ", nomStade=" + nomStade
                + ", ville=" + ville
                + ", capacite=" + capacite
                + ", dateConstruction=" + dateConstruction
                + ", pays=" + pays + "]";
    }
}
