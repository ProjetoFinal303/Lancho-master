package projetofinal.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Produto implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String nome;

    @SerializedName("description")
    private String descricao;

    @SerializedName("price")
    private BigDecimal preco;

    @SerializedName("imageUrl")
    private String imageUrl;

    public Produto() {}

    // Getters
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}