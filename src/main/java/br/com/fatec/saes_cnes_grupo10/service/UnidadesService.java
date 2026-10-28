package br.com.fatec.saes_cnes_grupo10.service;

import br.com.fatec.saes_cnes_grupo10.repository.UnidadesRepository;
import br.com.fatec.saes_cnes_grupo10.model.Unidades;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
public class UnidadesService {

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Selecione um arquivo para importar."
            );
        }

        String nomeArquivo = arquivo.getOriginalFilename();

        if (nomeArquivo == null) {
            throw new IllegalArgumentException(
                    "O arquivo selecionado é inválido."
            );
        }

        String nomeEmMinusculo = nomeArquivo.toLowerCase();

        if (!nomeEmMinusculo.endsWith(".txt")
                && !nomeEmMinusculo.endsWith(".csv")) {
            throw new IllegalArgumentException(
                    "Selecione um arquivo TXT ou CSV."
            );
        }
    }

    @Transactional
    public int importarArquivo(MultipartFile arquivo) {
        validarArquivo(arquivo);

        List<Unidades> UnidadessImportados = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(
                        arquivo.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String linha;
            int numeroLinha = 0;

            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;

                linha = linha.trim();

                if (linha.isEmpty()) {
                    throw new IllegalArgumentException(
                            "A linha " + numeroLinha + " está vazia."
                    );
                }

                String[] dados = linha.split(";", -1);

                if (dados.length != 2) {
                    throw new IllegalArgumentException(
                            "Formato inválido na linha " + numeroLinha
                                    + ". Utilize nome;idade."
                    );
                }

                String nome = dados[0].trim();
                String idadeTexto = dados[1].trim();

                if (numeroLinha == 1) {
                    nome = nome.replace("\uFEFF", "");
                }

                if (nome.isEmpty()) {
                    throw new IllegalArgumentException(
                            "O nome está vazio na linha " + numeroLinha + "."
                    );
                }

                int idade;

                try {
                    idade = Integer.parseInt(idadeTexto);
                } catch (NumberFormatException erro) {
                    throw new IllegalArgumentException(
                            "A idade da linha " + numeroLinha
                                    + " deve ser um número inteiro."
                    );
                }

                if (idade < 0 || idade > 180) {
                    throw new IllegalArgumentException(
                            "A idade da linha " + numeroLinha
                                    + " deve estar entre 0 e 180 anos."
                    );
                }

                Unidades Unidades = new Unidades();
                Unidades.setNome(nome);

                UnidadessImportados.add(Unidades);
            }

            if (UnidadessImportados.isEmpty()) {
                throw new IllegalArgumentException(
                        "O arquivo não contém usuários."
                );
            }

            UnidadesRepository.saveAll(UnidadessImportados);

            return UnidadessImportados.size();

        } catch (IllegalArgumentException erro) {
            throw erro;

        } catch (Exception erro) {
            throw new IllegalArgumentException(
                    "Não foi possível ler o arquivo."
            );
        }
    }

    private final UnidadesRepository UnidadesRepository;

    public UnidadesService(UnidadesRepository UnidadesRepository) {
        this.UnidadesRepository = UnidadesRepository;
    }

    public List<Unidades> listar() {
        return UnidadesRepository.findAll(Sort.by("id").ascending());
    }

    public Optional<Unidades> buscarPorId(Long id) {
        return UnidadesRepository.findById(id);
    }

    public Unidades salvar(Unidades Unidades) {
        return UnidadesRepository.save(Unidades);
    }

    public void deletar(Long id) {
        UnidadesRepository.deleteById(id);
    }

    public Optional<Unidades> atualizar(long id, Unidades
            UnidadesAtualizado) {
        return UnidadesRepository.findById(id).map(UnidadesExistente -> {
            UnidadesExistente.setNome(UnidadesAtualizado.getNome());
            return UnidadesRepository.save(UnidadesExistente);
        });
    }
}
