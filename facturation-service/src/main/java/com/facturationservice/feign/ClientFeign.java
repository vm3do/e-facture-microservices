package com.facturationservice.feign;

import com.facturationservice.dto.ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "client-service")
public interface ClientFeign {

    @GetMapping("/api/clients/{id}")
    ClientDTO getClientById(@PathVariable("id") Long id);
}

