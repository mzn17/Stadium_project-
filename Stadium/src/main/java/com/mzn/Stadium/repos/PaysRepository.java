package com.mzn.Stadium.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mzn.Stadium.entities.Pays;

@RepositoryRestResource(path = "pays", collectionResourceRel = "pays")
@CrossOrigin(origins = "http://localhost:4200",
             methods = { RequestMethod.GET, RequestMethod.POST,
                         RequestMethod.PUT, RequestMethod.DELETE })
public interface PaysRepository extends JpaRepository<Pays, Long> {

}