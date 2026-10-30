package br.com.fatec.saes_cnes_grupo10.service;

import br.com.fatec.saes_cnes_grupo10.model.Unidades;
import br.com.fatec.saes_cnes_grupo10.repository.UnidadesRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UnidadesService {
    private final UnidadesRepository unidadesRepository;

    public UnidadesService(UnidadesRepository unidadesRepository) {
        this.unidadesRepository = unidadesRepository;
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo para importar.");
        }

        String nomeArquivo = arquivo.getOriginalFilename();

        if (nomeArquivo == null || (!nomeArquivo.toLowerCase().endsWith(".csv") && !nomeArquivo.toLowerCase().endsWith(".txt"))) {
            throw new IllegalArgumentException("Selecione um arquivo CSV ou TXT.");
        }
    }

    @Transactional
    public Map<String, Integer> importarArquivo(MultipartFile arquivo) {
        validarArquivo(arquivo);

        int lidas = 0;
        int rejeitadas = 0;
        int duplicadas = 0;

        List<Unidades> importadas = new ArrayList<>();
        Set<String> codigosDoArquivo = new HashSet<>();

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            String cabecalho = leitor.readLine();

            if (cabecalho == null) {
                throw new IllegalArgumentException("O arquivo está vazio.");
            }

            cabecalho = cabecalho.replace("\uFEFF", "").trim();
            String esperado = "codigoCnes;nome;UF;municipio;tipoEstabelecimento;" + "esferaAdministrativa;atendeSus;latitude;longitude";

            if (!cabecalho.equalsIgnoreCase(esperado)) {
                throw new IllegalArgumentException("Cabeçalho inválido. Confira o formato do CSV.");
            }

            String linha;

            while ((linha = leitor.readLine()) != null) {
                lidas++;

                if (linha.trim().isEmpty()) {
                    rejeitadas++;
                    continue;
                }

                try {
                    String[] dados = linha.split(";", -1);

                    if (dados.length != 9) {
                        rejeitadas++;
                        continue;
                    }

                    String codigo = dados[0].trim();
                    String nome = dados[1].trim();
                    String uf = dados[2].trim().toUpperCase();
                    String municipio = dados[3].trim();
                    String tipo = dados[4].trim();
                    String esfera = dados[5].trim();
                    String atendeTexto = dados[6].trim().toLowerCase(Locale.ROOT);
                    double latitude = Double.parseDouble(dados[7].trim().replace(",", "."));
                    double longitude = Double.parseDouble(dados[8].trim().replace(",", "."));

                    if (codigo.isEmpty() || nome.isEmpty() || uf.isEmpty() || municipio.isEmpty()) {
                        rejeitadas++;
                        continue;
                    }

                    if (!codigosDoArquivo.add(codigo) || unidadesRepository.existsByCodigoCnes(codigo)) {
                        duplicadas++;
                        continue;
                    }

                    boolean atendeSus;

                    if (atendeTexto.equals("true") || atendeTexto.equals("sim") || atendeTexto.equals("1")) {
                        atendeSus = true;
                    } else if (atendeTexto.equals("false") || atendeTexto.equals("nao") || atendeTexto.equals("não") || atendeTexto.equals("0")) {
                        atendeSus = false;
                    } else {
                        rejeitadas++;
                        continue;
                    }

                    Unidades unidade = new Unidades();
                    unidade.setCodigoCnes(codigo);
                    unidade.setNome(nome);
                    unidade.setUF(uf);
                    unidade.setmunicipio(municipio);
                    unidade.setTipoEstabelecimento(tipo);
                    unidade.setEsferaAdministrativa(esfera);
                    unidade.setAtendeSus(atendeSus);
                    unidade.setLatitude(latitude);
                    unidade.setLongitude(longitude);

                    importadas.add(unidade);

                } catch (IllegalArgumentException erro) {
                    rejeitadas++;
                }
            }

            unidadesRepository.saveAll(importadas);
        } catch (IOException erro) {
            throw new IllegalArgumentException(
                "Não foi possível ler o arquivo."
            );
        }

        Map<String, Integer> resultado = new LinkedHashMap<>();
        resultado.put("lidas", lidas);
        resultado.put("importadas", importadas.size());
        resultado.put("rejeitadas", rejeitadas);
        resultado.put("duplicadas", duplicadas);

        return resultado;
    }

    
    public List<Unidades> filtrar(String nome, String categoria, String codigoCnes, String UF, String municipio, String tipoEstabelecimento, 
        String esferaAdministrativa, Boolean atendeSus, Double latitude, Double longitude) {
        List<Unidades> resultado = unidadesRepository.findAll();

        return resultado.stream().filter(u -> nome == null || nome.isBlank() || (u.getNome() != null && u.getNome().toLowerCase().contains(nome.toLowerCase())))
            .filter(u -> codigoCnes == null || codigoCnes.isBlank() || (u.getCodigoCnes() != null && u.getCodigoCnes().contains(codigoCnes)))
            .filter(u -> UF == null || UF.isBlank() || (u.getUF() != null && u.getUF().equalsIgnoreCase(UF)))
            .filter(u -> municipio == null || municipio.isBlank() || (u.getmunicipio() != null && u.getmunicipio().toLowerCase().contains(municipio.toLowerCase())))
            .filter(u -> tipoEstabelecimento == null || tipoEstabelecimento.isBlank() || (u.getTipoEstabelecimento() != null && u.getTipoEstabelecimento().equalsIgnoreCase(tipoEstabelecimento)))
            .filter(u -> esferaAdministrativa == null || esferaAdministrativa.isBlank() || (u.getEsferaAdministrativa() != null && u.getEsferaAdministrativa().equalsIgnoreCase(esferaAdministrativa)))
            .filter(u -> atendeSus == null || u.isAtendeSus() == atendeSus)
            .filter(u -> categoria == null || categoria.isBlank() || (u.getTipoEstabelecimento() != null && u.getTipoEstabelecimento().equalsIgnoreCase(categoria)))
            .filter(u -> latitude == null || (u.getLatitude() != null && u.getLatitude().equals(latitude)))
            .filter(u -> longitude == null || (u.getLongitude() != null && u.getLongitude().equals(longitude))).toList();
    }

    public List<Unidades> listar() {
        return unidadesRepository.findAll(
            Sort.by("id").ascending()
        );
    }

    public Optional<Unidades> buscarPorId(Long id) {
        return unidadesRepository.findById(id);
    }

    public Unidades salvar(Unidades unidade) {
        return unidadesRepository.save(unidade);
    }

    public void deletar(Long id) {
        unidadesRepository.deleteById(id);
    }

    public Optional<Unidades> atualizar(long id, Unidades unidadeAtualizada) {
        return unidadesRepository.findById(id).map(existente -> {
            existente.setNome(unidadeAtualizada.getNome());
            existente.setCodigoCnes(unidadeAtualizada.getCodigoCnes());
            existente.setUF(unidadeAtualizada.getUF());
            existente.setmunicipio(unidadeAtualizada.getmunicipio());
            existente.setTipoEstabelecimento(unidadeAtualizada.getTipoEstabelecimento());
            existente.setEsferaAdministrativa(unidadeAtualizada.getEsferaAdministrativa());
            existente.setAtendeSus(unidadeAtualizada.isAtendeSus());
            existente.setLatitude(unidadeAtualizada.getLatitude());
            existente.setLongitude(unidadeAtualizada.getLongitude());

            return unidadesRepository.save(existente);
        });
    }
}
