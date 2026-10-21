package com.mzn.Stadium.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mzn.Stadium.entities.Pays;
import com.mzn.Stadium.entities.Stade;
import com.mzn.Stadium.repos.StadeRepository;

@Service
public class StadeServiceImpl implements StadeService {

    @Autowired
    private StadeRepository stadeRepository;

    @Override
    public Stade saveStade(Stade s) {
        return stadeRepository.save(s);
    }

    @Override
    public Stade updateStade(Stade s) {
        return stadeRepository.save(s);
    }

    @Override
    public void deleteStade(Stade s) {
        stadeRepository.delete(s);
    }

    @Override
    public void deleteStadeById(Long id) {
        stadeRepository.deleteById(id);
    }

    @Override
    public Stade getStade(Long id) {
        return stadeRepository.findById(id).orElse(null);
    }

    @Override
    public List<Stade> getAllStades() {
        return stadeRepository.findAll();
    }

    @Override
    public List<Stade> findByNomStade(String nom) {
        return stadeRepository.findByNomStade(nom);
    }

    @Override
    public List<Stade> findByNomStadeContains(String nom) {
        return stadeRepository.findByNomStadeContains(nom);
    }

    @Override
    public List<Stade> findByNomCapacite(String nom, Integer capacite) {
        return stadeRepository.findByNomCapacite(nom, capacite);
    }

    @Override
    public List<Stade> findByPays(Pays pays) {
        return stadeRepository.findByPays(pays);
    }

    @Override
    public List<Stade> findByPaysIdPays(Long id) {
        return stadeRepository.findByPaysIdPays(id);
    }

    @Override
    public List<Stade> findByOrderByNomStadeAsc() {
        return stadeRepository.findByOrderByNomStadeAsc();
    }

    @Override
    public List<Stade> trierStadesNomsCapacite() {
        return stadeRepository.trierStadesNomsCapacite();
    }
}