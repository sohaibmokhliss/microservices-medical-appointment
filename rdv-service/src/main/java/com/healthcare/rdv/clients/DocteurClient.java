package com.healthcare.rdv.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "docteur-service")
public interface DocteurClient {

    @GetMapping("/api/docteurs/{id}")
    DocteurDTO getDocteur(@PathVariable("id") Long id);
}
