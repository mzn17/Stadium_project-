package com.mzn.Stadium.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.mzn.Stadium.entities.Pays;
import com.mzn.Stadium.entities.Stade;

@RepositoryRestResource(path = "rest", exported = false)
public interface StadeRepository extends JpaRepository<Stade, Long> {

    // 1. Recherche exacte par nom
    List<Stade> findByNomStade(String nom);

    // 2. Recherche avec Contains
    List<Stade> findByNomStadeContains(String nom);

    // 3. Recherche avec @Query
    @Query("select s from Stade s where s.nomStade like %?1 and s.capacite > ?2")
    List<Stade> findByNomCapacite(String nom, Integer capacite);

    // 4. Recherche avec @Query et un objet Pays
    @Query("select s from Stade s where s.pays = ?1")
    List<Stade> findByPays(Pays pays);

    // 5. Recherche des stades par ID du pays
    List<Stade> findByPaysIdPays(Long id);

    // 6. Tri par nom croissant
    List<Stade> findByOrderByNomStadeAsc();

    // 7. Tri par nom ASC et capacité DESC
    @Query("select s from Stade s order by s.nomStade ASC, s.capacite DESC")
    List<Stade> trierStadesNomsCapacite();
}