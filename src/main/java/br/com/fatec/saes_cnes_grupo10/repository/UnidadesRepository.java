package br.com.fatec.saes_cnes_grupo10.repository;

import br.com.fatec.saes_cnes_grupo10.model.Unidades;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnidadesRepository extends JpaRepository<Unidades, Long> {
    boolean existsByCodigoCnes(String codigoCnes);
    List<Unidades> findByNomeContainingIgnoreCase(String nome);
    List<Unidades> findByMunicipioContainingIgnoreCase(String municipio);
    List<Unidades> findByUFIgnoreCase(String uf);
}