package br.com.fatec.saes_cnes_grupo10.controller;

import br.com.fatec.saes_cnes_grupo10.model.Unidades;
import br.com.fatec.saes_cnes_grupo10.service.UnidadesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/Unidades")
public class UnidadesController {

    private final UnidadesService UnidadesService;

    public UnidadesController(UnidadesService UnidadesService) {
        this.UnidadesService = UnidadesService;
    }

    @GetMapping
    public ResponseEntity<List<Unidades>> listar() {
        return ResponseEntity.ok().body(UnidadesService.listar());
    }

    @PostMapping
    public ResponseEntity<Unidades> salvar(@RequestBody Unidades Unidades) {
        Unidades novoUnidades = UnidadesService.salvar(Unidades);
        return ResponseEntity.ok(novoUnidades);
    }

    @PostMapping("/importar")
    public ResponseEntity<String> importarArquivo(
            @RequestParam("arquivo") MultipartFile arquivo) {
        try {
            int quantidade = UnidadesService.importarArquivo(arquivo);
            return ResponseEntity.ok(
                    quantidade + " usuários importados com sucesso."
            );

        } catch (IllegalArgumentException erro) {
            return ResponseEntity
                    .badRequest()
                    .body("Arquivo inválido: " + erro.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Unidades> buscarPorId(@PathVariable Long id) {
        Optional<Unidades> Unidades = UnidadesService.buscarPorId(id);
        return Unidades.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable long id) {
        if (UnidadesService.buscarPorId(id).isPresent()) {
            UnidadesService.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Unidades> atualizar(
            @PathVariable("id") long id,
            @RequestBody Unidades Unidades) {
        return UnidadesService.atualizar(id, Unidades)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    //ADICIONAR FILTRO!
    @GetMapping("/filtro")
    public ResponseEntity<List<String>> filtrar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String codigoCnes,
            @RequestParam(required = false) String UF,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String tipoEstabelecimento,
            @RequestParam(required = false) String esferaAdministrativa,
            @RequestParam(required = false) boolean atendeSus,
            @RequestParam(required = false) long[] latitudeLongitude) {

        // Lógica de filtragem integrada aqui
        List<Unidades> unidades = UnidadesService.filtrar(nome, categoria, codigoCnes, UF,
                esferaAdministrativa, atendeSus, latitudeLongitude);

        if (unidades.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(Unidades);
    }
}
