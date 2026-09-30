package cl.duoc.pixelrig.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "aplicacion", "PixelRig API",
                "version", "1.0",
                "descripcion", "API de componentes de PC",
                "estado", "operativo"
        );
    }
}
