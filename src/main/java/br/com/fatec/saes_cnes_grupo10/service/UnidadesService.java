package br.com.fatec.saes_cnes_grupo10.service;

import br.com.fatec.saes_cnes_grupo10.model.Unidades;
import br.com.fatec.saes_cnes_grupo10.repository.UnidadesRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Service
public class UnidadesService {
    private final UnidadesRepository unidadesRepository;

    public UnidadesService(UnidadesRepository unidadesRepository) {
        this.unidadesRepository = unidadesRepository;
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo.");
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || (!nome.toLowerCase(Locale.ROOT).endsWith(".csv") && !nome.toLowerCase(Locale.ROOT).endsWith(".txt"))) {
            throw new IllegalArgumentException("Formato inválido. Envie um arquivo CSV ou TXT.");
        }
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto.replace("\uFEFF", ""), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private String obterValor(Map<String, Integer> colunas, CSVRecord registro, String... alternativas) {
        for (String alternativa : alternativas) {
            Integer indice = colunas.get(normalizar(alternativa));
            if (indice != null && indice < registro.size()) {
                return registro.get(indice).trim();
            }
        }
        return "";
    }

    public Map<String, Integer> importarArquivo(MultipartFile arquivo) {
        validarArquivo(arquivo);
        int lidas = 0;
        int rejeitadas = 0;
        int duplicadas = 0;
        int importadasTotal = 0;

        List<Unidades> importadas = new ArrayList<>();
        Set<String> codigosDoArquivo = new HashSet<>();
        Set<String> codigosNoBanco = unidadesRepository.buscarTodosCodigosCnes();

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(arquivo.getInputStream(), StandardCharsets.ISO_8859_1))) {
            leitor.mark(8192);
            String primeiraLinha = leitor.readLine();
            if (primeiraLinha == null || primeiraLinha.isBlank()) {
                throw new IllegalArgumentException("Arquivo vazio.");
            }
            leitor.reset();
            primeiraLinha = primeiraLinha.replace("\uFEFF", "");
            String separador = primeiraLinha.chars().filter(c -> c == ';').count() >= primeiraLinha.chars().filter(c -> c == ',').count() ? ";" : ",";

            CSVFormat formato = CSVFormat.DEFAULT.builder().setDelimiter(separador.charAt(0)).setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true).build();

            try (CSVParser parser = formato.parse(leitor)) {
                Map<String, Integer> colunas = new HashMap<>();
                parser.getHeaderMap().forEach((nome, indice) -> colunas.put(normalizar(nome), indice));

                if (!colunas.containsKey("cocnes") && !colunas.containsKey("codigocnes") && !colunas.containsKey("cnes")) {
                    throw new IllegalArgumentException("O arquivo não possui a coluna de código CNES.");
                }

                for (CSVRecord registro : parser) {
                    lidas++;
                    try {
                        String codigo = obterValor(colunas, registro, "CO_CNES", "codigoCnes", "CNES");
                        String nome = obterValor(colunas, registro, "NO_FANTASIA", "nome", "nomeFantasia");
                        String uf = traduzirUF(obterValor(colunas, registro, "CO_ESTADO_GESTOR", "CO_UF", "UF", "SG_UF"));
                        String municipio = obterValor(colunas, registro, "CO_MUNICIPIO_GESTOR", "CO_MUNICIPIO", "municipio");
                        String tipo = traduzirTipo(obterValor(colunas, registro, "TP_UNIDADE", "CO_TIPO_UNIDADE", "tipoEstabelecimento"));
                        String esfera = traduzirEsfera(obterValor(colunas, registro, "CO_NATUREZA_JUR", "TP_GESTAO", "esferaAdministrativa"));
                        String latitudeTexto = obterValor(colunas, registro, "NU_LATITUDE", "latitude");
                        String longitudeTexto = obterValor(colunas, registro, "NU_LONGITUDE", "longitude");

                        Double latitude = latitudeTexto.isBlank() ? null : Double.parseDouble(latitudeTexto.replace(",", "."));
                        Double longitude = longitudeTexto.isBlank() ? null : Double.parseDouble(longitudeTexto.replace(",", "."));

                        if (codigo.isBlank() || nome.isBlank()) {
                            rejeitadas++;
                            continue;
                        }

                        if (!codigosDoArquivo.add(codigo) || codigosNoBanco.contains(codigo)) {
                            duplicadas++;
                            continue;
                        }

                        Unidades unidade = new Unidades();
                        unidade.setCodigoCnes(codigo);
                        unidade.setNome(nome);
                        unidade.setUF(uf);
                        unidade.setMunicipio(municipio);
                        unidade.setTipoEstabelecimento(tipo);
                        unidade.setEsferaAdministrativa(esfera);
                        unidade.setLatitude(latitude);
                        unidade.setLongitude(longitude);

                        importadas.add(unidade);
                        importadasTotal++;

                    } catch (Exception erro) {
                        rejeitadas++;
                    }

                    if (importadas.size() >= 100) {
                        try {
                            unidadesRepository.saveAll(importadas);
                        } catch (Exception erroBanco) {
                            rejeitadas += importadas.size();
                            importadasTotal -= importadas.size();
                        } finally {
                            importadas.clear();
                        }
                    }

                    if (importadasTotal >= 500) {
                        break;
                    }
                }
            }

            if (!importadas.isEmpty()) {
                try {
                    unidadesRepository.saveAll(importadas);
                } catch (Exception erroBanco) {
                    rejeitadas += importadas.size();
                    importadasTotal -= importadas.size();
                }
            }
        } catch (IOException erro) {
            throw new IllegalArgumentException("Não foi possível realizar a leitura do arquivo.");
        }

        Map<String, Integer> resultado = new LinkedHashMap<>();
        resultado.put("lidas", lidas);
        resultado.put("importadas", importadasTotal);
        resultado.put("rejeitadas", rejeitadas);
        resultado.put("duplicadas", duplicadas);

        return resultado;
    }

    public List<Unidades> filtrar(String nome, String categoria, String codigoCnes, String UF, String municipio, String tipoEstabelecimento, String esferaAdministrativa, Double latitude, Double longitude) {
        List<Unidades> resultado = unidadesRepository.findAll();
        
        return resultado.stream().filter(u -> nome == null || nome.isBlank() || (u.getNome() != null && u.getNome().toLowerCase().contains(nome.toLowerCase())))
            .filter(u -> codigoCnes == null || codigoCnes.isBlank() || (u.getCodigoCnes() != null && u.getCodigoCnes().contains(codigoCnes)))
            .filter(u -> UF == null || UF.isBlank() || (u.getUF() != null && u.getUF().equalsIgnoreCase(UF)))
            .filter(u -> municipio == null || municipio.isBlank() || (u.getMunicipio() != null && u.getMunicipio().toLowerCase().contains(municipio.toLowerCase())))
            .filter(u -> tipoEstabelecimento == null || tipoEstabelecimento.isBlank() || (u.getTipoEstabelecimento() != null && u.getTipoEstabelecimento().equalsIgnoreCase(tipoEstabelecimento)))
            .filter(u -> esferaAdministrativa == null || esferaAdministrativa.isBlank() || (u.getEsferaAdministrativa() != null && u.getEsferaAdministrativa().equalsIgnoreCase(esferaAdministrativa)))
            .filter(u -> categoria == null || categoria.isBlank() || (u.getTipoEstabelecimento() != null && u.getTipoEstabelecimento().equalsIgnoreCase(categoria)))
            .toList();
    }

    public List<Unidades> listar() {
        return unidadesRepository.findAll(Sort.by("id").ascending());
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
            existente.setMunicipio(unidadeAtualizada.getMunicipio());
            existente.setTipoEstabelecimento(unidadeAtualizada.getTipoEstabelecimento());
            existente.setEsferaAdministrativa(unidadeAtualizada.getEsferaAdministrativa());

            return unidadesRepository.save(existente);
        });
    }

    private String traduzirUF(String codigo) {
        Map<String, String> ufs = Map.ofEntries(Map.entry("11", "RO"), Map.entry("12", "AC"), Map.entry("13", "AM"), Map.entry("14", "RR"), 
        Map.entry("15", "PA"), Map.entry("16", "AP"), Map.entry("17", "TO"), Map.entry("21", "MA"), Map.entry("22", "PI"), Map.entry("23", "CE"),
        Map.entry("24", "RN"), Map.entry("25", "PB"), Map.entry("26", "PE"), Map.entry("27", "AL"), Map.entry("28", "SE"), Map.entry("29", "BA"),
        Map.entry("31", "MG"), Map.entry("32", "ES"), Map.entry("33", "RJ"), Map.entry("35", "SP"), Map.entry("41", "PR"), Map.entry("42", "SC"), 
        Map.entry("43", "RS"), Map.entry("50", "MS"), Map.entry("51", "MT"), Map.entry("52", "GO"), Map.entry("53", "DF"));

        return ufs.getOrDefault(codigo.trim(), codigo);
    }

    private String traduzirEsfera(String codigo) {
            Map<String, String> esferas = Map.ofEntries(Map.entry("1244", "Município"), Map.entry("2000", "Entidades Empresariais"),
                Map.entry("2062", "Sociedade Empresária Limitada"), Map.entry("1236", "Estado ou Distrito Federal"),
                Map.entry("2011", "Empresa Pública"), Map.entry("2038", "Sociedade de Economia Mista"),
                Map.entry("2046", "Sociedade Anônima Aberta"), Map.entry("2054", "Sociedade Anônima Fechada"),
                Map.entry("3999", "Associação Privada"),Map.entry("3069", "Fundação Privada"), Map.entry("4000", "Pessoa Física"), 
                Map.entry("3131", "Entidade Sindical"), Map.entry("2135", "Empresário (Individual)"));

            return esferas.getOrDefault(codigo.trim(), codigo);
        }

    private String traduzirTipo(String codigo) {
        Map<String, String> tipos = Map.ofEntries(
            Map.entry("1", "Posto de Saúde"), Map.entry("2", "Centro de Saúde / Unidade Básica"), Map.entry("4", "Policlínica"),
            Map.entry("5", "Hospital Geral"), Map.entry("7", "Hospital Especializado"), Map.entry("15", "Unidade Mista"),
            Map.entry("20", "Pronto-Socorro Geral"), Map.entry("21", "Pronto-Socorro Especializado"), Map.entry("22", "Consultório Isolado"),
            Map.entry("32", "Unidade Móvel Fluvial"), Map.entry("36", "Clínica / Centro de Especialidade"), 
            Map.entry("39", "Unidade de Apoio Diagnose e Terapia"), Map.entry("40", "Unidade Móvel Terrestre"),
            Map.entry("42", "Unidade Móvel Pré-Hospitalar de Urgência"), Map.entry("43", "Farmácia"), Map.entry("50", "Unidade de Vigilância em Saúde"),
            Map.entry("60", "Cooperativa ou Empresa de Cessão de Trabalhadores"), Map.entry("61", "Centro de Parto Normal"), Map.entry("62", "Hospital-Dia"),
            Map.entry("64", "Central de Regulação de Serviços de Saúde"), Map.entry("67", "Laboratório Central de Saúde Pública"),
            Map.entry("68", "Central de Gestão em Saúde"), Map.entry("69", "Centro de Atenção Hemoterápica"), Map.entry("70", "Centro de Atenção Psicossocial"),
            Map.entry("71", "Centro de Apoio à Saúde da Família"), Map.entry("72", "Unidade de Atenção à Saúde Indígena"), Map.entry("73", "Pronto Atendimento"),
            Map.entry("74", "Polo Academia da Saúde"), Map.entry("75", "Telessaúde"), Map.entry("76", "Central de Regulação Médica das Urgências"),
            Map.entry("77", "Serviço de Atenção Domiciliar"), Map.entry("78", "Unidade de Atenção em Regime Residencial"), Map.entry("79", "Oficina Ortopédica"),
            Map.entry("80", "Laboratório de Saúde Pública"), Map.entry("81", "Central de Regulação do Acesso"), Map.entry("82", "Central de Notificação e Captação de Órgãos"),
            Map.entry("83", "Polo de Prevenção e Promoção da Saúde"), Map.entry("84", "Central de Abastecimento"), Map.entry("85", "Centro de Imunização"));

            if (codigo == null || codigo.isBlank()) {
                return "";
            }

            String chave = codigo.trim();

            try {
                chave = String.valueOf(Integer.parseInt(chave));
            } catch (NumberFormatException e) {
                return codigo;
            }

            return tipos.getOrDefault(chave, codigo);
    }
}
