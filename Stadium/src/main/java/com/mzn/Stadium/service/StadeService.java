package com.mzn.Stadium.service;

import java.util.List;

import com.mzn.Stadium.entities.Pays;
import com.mzn.Stadium.entities.Stade;

public interface StadeService {


    Stade saveStade(Stade s);

    Stade updateStade(Stade s);

    void deleteStade(Stade s);

    void deleteStadeById(Long id);

    Stade getStade(Long id);

    List<Stade> getAllStades();

    List<Stade> findByNomStade(String nom);

    List<Stade> findByNomStadeContains(String nom);

    List<Stade> findByNomCapacite(String nom, Integer capacite);

    List<Stade> findByPays(Pays pays);

    List<Stade> findByPaysIdPays(Long id);

    List<Stade> findByOrderByNomStadeAsc();

    List<Stade> trierStadesNomsCapacite();
}