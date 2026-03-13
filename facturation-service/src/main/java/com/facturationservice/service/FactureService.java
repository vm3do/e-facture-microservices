package com.facturationservice.service;

import com.facturationservice.dto.CreateFactureRequest;
import com.facturationservice.dto.FactureEnrichedResponse;
import com.facturationservice.dto.FactureResponse;

import java.util.List;

public interface FactureService {
    FactureResponse createFacture(CreateFactureRequest request);

    FactureEnrichedResponse getFactureById(Long id);
}

