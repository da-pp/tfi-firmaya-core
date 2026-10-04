package com.firmaya.core.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.PlantillaRequest;
import com.firmaya.core.dto.PlantillaResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.PlantillaService;

@RestController
@RequestMapping("/api")
public class PlantillaController {

    private final PlantillaService plantillaService;

    public PlantillaController(PlantillaService plantillaService) {
        this.plantillaService = plantillaService;
    }

    // CU-01 – Crear contrato desde plantilla: plantillas activas disponibles
    @GetMapping("/plantillas/activas")
    public List<PlantillaResponse> listarPlantillasActivas() {
        return plantillaService.listarPlantillasActivas();
    }

    // CU-16 – Gestionar plantillas de contrato (BackOffice)
    @GetMapping("/admin/plantillas")
    public List<PlantillaResponse> listarPlantillas() {
        return plantillaService.listarPlantillas();
    }

    @GetMapping("/admin/plantillas/{idPlantilla}")
    public PlantillaResponse obtenerPlantilla(@PathVariable Integer idPlantilla) {
        return plantillaService.obtenerPlantilla(idPlantilla);
    }

    @PostMapping("/admin/plantillas")
    @ResponseStatus(HttpStatus.CREATED)
    public PlantillaResponse crearPlantilla(@Valid @RequestBody PlantillaRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario administrador,
            HttpServletRequest httpRequest) {
        return plantillaService.crearPlantilla(request, administrador, httpRequest.getRemoteAddr());
    }

    @PutMapping("/admin/plantillas/{idPlantilla}")
    public PlantillaResponse editarPlantilla(@PathVariable Integer idPlantilla,
            @Valid @RequestBody PlantillaRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario administrador,
            HttpServletRequest httpRequest) {
        return plantillaService.editarPlantilla(idPlantilla, request, administrador, httpRequest.getRemoteAddr());
    }
}
