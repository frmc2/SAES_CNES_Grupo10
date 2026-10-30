package br.com.fatec.saes_cnes_grupo10.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Unidades")
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
    private Double latitude;
    private Double longitude;

    public Unidades() {
    }

    public Unidades(String nome, String codigoCnes, String UF, String municipio, String tipoEstabelecimento, String esferaAdministrativa, 
        boolean atendeSus, Double latitude, Double longitude) {
        this.nome = nome;
        this.codigoCnes = codigoCnes;
        this.UF = UF;
        this.municipio = municipio;
        this.tipoEstabelecimento = tipoEstabelecimento;
        this.esferaAdministrativa = esferaAdministrativa;
        this.atendeSus = atendeSus;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
