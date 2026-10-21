package com.mzn.Stadium.entities;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "nomStade", types = { Stade.class })
public interface StadeProjection {

    public String getNomStade();
}