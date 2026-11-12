package com.mzn.Stadium.restcontrollers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mzn.Stadium.entities.Stade;
import com.mzn.Stadium.service.StadeService;

@RestController
// ---------------------------------------------------------------
// 1️⃣  The base path now matches the Angular constant:
//     environment.apiURL = "http://localhost:8080/stades/api"
//     → final endpoint = http://localhost:8080/stades/api
// ---------------------------------------------------------------
@RequestMapping("/api")
@CrossOrigin("*")                     // Allows requests from http://localhost:4200
public class StadeRESTController {

    @Autowired
    private StadeService stadeService;

    // -----------------------------------------------------------
    // 2️⃣  End‑points (relative to "/api")
    // -----------------------------------------------------------

    // 1. Return all stades
    //    GET http://localhost:8080/stades/api
    @GetMapping()
    public List<Stade> getAllStades() {
        return stadeService.getAllStades();
    }

    // 2. Get a stade by its ID
    //    GET http://localhost:8080/stades/api/{id}
    @GetMapping("/{id}")
    public Stade getStadeById(@PathVariable("id") Long id) {
        return stadeService.getStade(id);
    }

    // 3. Create a new stade
    //    POST http://localhost:8080/stades/api
    @PostMapping()
    public Stade createStade(@RequestBody Stade stade) {
        return stadeService.saveStade(stade);
    }

    // 4. Update an existing stade
    //    PUT http://localhost:8080/stades/api
    //    (the request body must contain the existing `idStade`)
    @PutMapping()
    public Stade updateStade(@RequestBody Stade stade) {
        return stadeService.updateStade(stade);
    }

    // 5. Delete a stade
    //    DELETE http://localhost:8080/stades/api/{id}
    @DeleteMapping("/{id}")
    public void deleteStade(@PathVariable("id") Long id) {
        stadeService.deleteStadeById(id);
    }

    // 6. Return stades of a given country
    //    GET http://localhost:8080/stades/api/stadespays/{idPays}
    @RequestMapping(value = "/stadespays/{idPays}", method = RequestMethod.GET)
    public List<Stade> getStadesByPaysId(@PathVariable("idPays") Long idPays) {
        return stadeService.findByPaysIdPays(idPays);
    }

    // 7. Search stades whose name contains the given string
    //    GET http://localhost:8080/stades/api/stadesByName/{nom}
    @RequestMapping(value = "/stadesByName/{nom}", method = RequestMethod.GET)
    public List<Stade> findByNomStadeContains(@PathVariable("nom") String nom) {
        return stadeService.findByNomStadeContains(nom);
    }
}
