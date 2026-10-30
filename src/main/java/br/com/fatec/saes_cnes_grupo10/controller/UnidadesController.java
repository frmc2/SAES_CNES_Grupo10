package br.com.fatec.saes_cnes_grupo10.controller;

import br.com.fatec.saes_cnes_grupo10.model.Unidades;
import br.com.fatec.saes_cnes_grupo10.service.UnidadesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/Unidades")
public class UnidadesController {
    private final UnidadesService unidadesService;

    public UnidadesController(UnidadesService unidadesService) {
        this.unidadesService = unidadesService;
    }

    @GetMapping
    public ResponseEntity<List<Unidades>> listar() {
        return ResponseEntity.ok(unidadesService.listar());
    }

    @PostMapping
    public ResponseEntity<Unidades> salvar(@RequestBody Unidades unidade) {
        Unidades novaUnidade = unidadesService.salvar(unidade);
        return ResponseEntity.ok(novaUnidade);
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Integer>> importarArquivo(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            Map<String, Integer> resultado = unidadesService.importarArquivo(arquivo);

            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException erro) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Unidades> buscarPorId(@PathVariable Long id) {
        Optional<Unidades> unidade = unidadesService.buscarPorId(id);

        return unidade.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable long id) {
        if (unidadesService.buscarPorId(id).isPresent()) {
            unidadesService.deletar(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Unidades> atualizar(
            @PathVariable("id") long id,
            @RequestBody Unidades unidade) {

        return unidadesService.atualizar(id, unidade).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/filtro")
    public ResponseEntity<List<Unidades>> filtrar(@RequestParam(required = false) String nome, @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String codigoCnes, @RequestParam(required = false) String UF, 
            @RequestParam(required = false) String municipio, @RequestParam(required = false) String tipoEstabelecimento, 
            @RequestParam(required = false) String esferaAdministrativa, @RequestParam(required = false) Boolean atendeSus, 
            @RequestParam(required = false) Double latitude, @RequestParam(required = false) Double longitude) {

        List<Unidades> unidades = unidadesService.filtrar(nome, categoria, codigoCnes, UF, municipio, tipoEstabelecimento, esferaAdministrativa,
                atendeSus, latitude, longitude
        );

        if (unidades.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(unidades);
    }
}
