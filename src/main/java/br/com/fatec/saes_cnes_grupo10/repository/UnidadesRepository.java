package br.com.fatec.saes_cnes_grupo10.repository;

import br.com.fatec.saes_cnes_grupo10.model.Unidades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Set;

public interface UnidadesRepository extends JpaRepository<Unidades, Long> {
    
    boolean existsByCodigoCnes(String codigoCnes);

    @Query("SELECT u.codigoCnes FROM Unidades u")
    Set<String> buscarTodosCodigosCnes();
}