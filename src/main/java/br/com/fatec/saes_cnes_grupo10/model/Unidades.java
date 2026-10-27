package br.com.fatec.saes_cnes_grupo10.model;

import br.com.fatec.saes_cnes_grupo10.service.UnidadesService;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Unidades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nome;
    private String codigoCnes;
    private String UF;
    private String municipio;
    private String tipoEstabelecimento;
    private String esferaAdministrativa;
    private boolean atendeSus;
    private long[] latitudeLongitude = new long[2];

    public Unidades() {}

    public Unidades (String nome, String codigoCnes, String UF, String municipio,
                     String tipoEstabelecimento, String esferaAdministrativa,
                     boolean atendeSus, long[] latitudeLongitude) {
        this.nome = nome;
        this.codigoCnes = codigoCnes;
        this.UF = UF;
        this.municipio = municipio;
        this.tipoEstabelecimento = tipoEstabelecimento;
        this.esferaAdministrativa = esferaAdministrativa;
        this.atendeSus = atendeSus;
        this.latitudeLongitude = latitudeLongitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigoCnes() {
        return codigoCnes;
    }

    public void setCodigoCnes(String CodigoCnes) {
        this.codigoCnes = CodigoCnes;
    }

    public String getUF() {
        return UF;
    }

    public void setUF(String UF) {
        this.UF = UF;
    }

    public String getmunicipio() {
        return municipio;
    }

    public void setmunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getTipoEstabelecimento() {
        return tipoEstabelecimento;
    }

    public void setTipoEstabelecimento(String tipoEstabelecimento) {
        this.tipoEstabelecimento = tipoEstabelecimento;
    }

    public String getEsferaAdministrativa() {
        return esferaAdministrativa;
    }

    public void setEsferaAdministrativa(String esferaAdministrativa) {
        this.esferaAdministrativa = esferaAdministrativa;
    }

    public boolean isAtendeSus() {
        return atendeSus;
    }

    public void setAtendeSus(boolean atendeSus) {
        this.atendeSus = atendeSus;
    }

    public long[] getLatitudeLongitude() {
        return latitudeLongitude;
    }

    public void setLatitudeLongitude(long[] latitudeLongitude) {
        this.latitudeLongitude = latitudeLongitude;
    }

}
