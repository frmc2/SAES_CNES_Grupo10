package br.com.fatec.saes_cnes_grupo10.model;

import jakarta.persistence.*;

@Entity
@Table(name = "unidades")
public class Unidades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "NO_FANTASIA")
    private String nome;
    @Column(name = "CO_CNES", unique = true)
    private String codigoCnes;
    @Column(name = "CO_ESTADO_GESTOR")
    private String UF;
    @Column(name = "CO_MUNICIPIO_GESTOR")
    private String municipio;
    @Column(name = "TP_UNIDADE")
    private String tipoEstabelecimento;
    @Column(name = "CO_NATUREZA_JUR")
    private String esferaAdministrativa;
    @Column(name = "NU_LATITUDE")
    private Double latitude; 
    @Column(name = "NU_LONGITUDE")
    private Double longitude; 

    public Unidades() {}

    public Unidades(String nome, String codigoCnes, String UF, String municipio, String tipoEstabelecimento, String esferaAdministrativa, Double latitude, Double longitude) {
        this.nome = nome;
        this.codigoCnes = codigoCnes;
        this.UF = UF;
        this.municipio = municipio;
        this.tipoEstabelecimento = tipoEstabelecimento;
        this.esferaAdministrativa = esferaAdministrativa;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodigoCnes() { return codigoCnes; }
    public void setCodigoCnes(String codigoCnes) { this.codigoCnes = codigoCnes; }

    public String getUF() { return UF; }
    public void setUF(String UF) { this.UF = UF; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getTipoEstabelecimento() { return tipoEstabelecimento; }
    public void setTipoEstabelecimento(String tipoEstabelecimento) { this.tipoEstabelecimento = tipoEstabelecimento; }

    public String getEsferaAdministrativa() { return esferaAdministrativa; }
    public void setEsferaAdministrativa(String esferaAdministrativa) { this.esferaAdministrativa = esferaAdministrativa; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
