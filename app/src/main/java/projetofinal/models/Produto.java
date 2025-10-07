package projetofinal.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class Produto implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("descricao")
    private String descricao;

    @SerializedName("preco")
    private BigDecimal preco;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("stripe_price_id")
    private String stripePriceId;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("media_avaliacoes")
    private double mediaAvaliacoes;

    @SerializedName("total_avaliacoes")
    private int totalAvaliacoes;

    // NOVO CAMPO ADICIONADO PARA O ESTOQUE
    private int quantidadeEstoque;

    public Produto() {
    }

    public Produto(int id, String nome, String descricao, BigDecimal preco, String imageUrl, String categoria, double mediaAvaliacoes, int totalAvaliacoes) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imageUrl = imageUrl;
        this.categoria = categoria;
        this.mediaAvaliacoes = mediaAvaliacoes;
        this.totalAvaliacoes = totalAvaliacoes;
    }

    // Getters e Setters existentes
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStripePriceId() { return stripePriceId; }
    public void setStripePriceId(String stripePriceId) { this.stripePriceId = stripePriceId; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public double getMediaAvaliacoes() { return mediaAvaliacoes; }
    public void setMediaAvaliacoes(double mediaAvaliacoes) { this.mediaAvaliacoes = mediaAvaliacoes; }
    public int getTotalAvaliacoes() { return totalAvaliacoes; }
    public void setTotalAvaliacoes(int totalAvaliacoes) { this.totalAvaliacoes = totalAvaliacoes; }

    // GETTER E SETTER PARA O NOVO CAMPO DE ESTOQUE
    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(int quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }
}