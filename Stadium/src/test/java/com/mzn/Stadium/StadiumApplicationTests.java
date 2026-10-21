package com.mzn.Stadium;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mzn.Stadium.entities.Pays;
import com.mzn.Stadium.entities.Stade;
import com.mzn.Stadium.repos.StadeRepository;

@SpringBootTest
public class StadiumApplicationTests {

    @Autowired
    private StadeRepository stadeRepository;

    @Test
    public void testFindByNomStade() {

        List<Stade> stades =
                stadeRepository.findByNomStade("Stade Olympique de Radès");

        for (Stade s : stades) {
            System.out.println(s);
        }
    }

    @Test
    public void testFindByNomStadeContains() {

        List<Stade> stades =
                stadeRepository.findByNomStadeContains("Olympique");

        for (Stade s : stades) {
            System.out.println(s);
        }
    }

    @Test
    public void testFindByNomCapacite() {

        List<Stade> stades =
                stadeRepository.findByNomCapacite("Olympique", 30000);

        for (Stade s : stades) {
            System.out.println(s);
        }
    }

    @Test
    public void testFindByPays() {

        Pays pays = new Pays();

        pays.setIdPays(1L);

        List<Stade> stades =
                stadeRepository.findByPays(pays);

        for (Stade s : stades) {
            System.out.println(s);
        }
    }

    @Test
    public void testFindByPaysIdPays() {

        List<Stade> stades =
                stadeRepository.findByPaysIdPays(1L);

        for (Stade s : stades) {
            System.out.println(s);
        }
    }

    @Test
    public void testFindByOrderByNomStadeAsc() {

        List<Stade> stades =
                stadeRepository.findByOrderByNomStadeAsc();

        for (Stade s : stades) {
            System.out.println(s);
        }
    }

    @Test
    public void testTrierStadesNomsCapacite() {

        List<Stade> stades =
                stadeRepository.trierStadesNomsCapacite();

        for (Stade s : stades) {
            System.out.println(s);
        }
    }
}