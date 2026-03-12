package com.facturationservice.service;

import com.facturationservice.dto.CreateFactureRequest;
import com.facturationservice.dto.FactureResponse;

public interface FactureService {
    FactureResponse createFacture(CreateFactureRequest request);
}

