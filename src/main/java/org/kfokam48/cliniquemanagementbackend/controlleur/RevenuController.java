package org.kfokam48.cliniquemanagementbackend.controlleur;

import org.kfokam48.cliniquemanagementbackend.dto.RevenuDTO;
import org.kfokam48.cliniquemanagementbackend.service.RevenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/revenus")
public class RevenuController {

    @Autowired
    private RevenuService revenuService;

    @GetMapping
    public ResponseEntity<RevenuDTO> getRevenuMensuel() {
        RevenuDTO revenus = revenuService.getRevenuMensuel();
        return ResponseEntity.ok(revenus);
    }
}
