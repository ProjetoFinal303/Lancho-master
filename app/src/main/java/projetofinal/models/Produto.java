package projetofinal.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public class Produto implements Serializable {

    private int id;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    public Produto() {}

    public Produto(String nome, String descricao, String precoStr) {
        this.nome = nome;
        this.descricao = descricao;
        setPrecoFromString(precoStr);
    }

    // Construtor para ler do banco (double) e converter para BigDecimal
    public Produto(int id, String nome, String descricao, double precoDouble) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = BigDecimal.valueOf(precoDouble).setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }

    public void setPreco(BigDecimal preco) {
        this.preco = (preco != null) ? preco.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public void setPrecoFromString(String precoStr) {
        if (precoStr == null || precoStr.trim().isEmpty()) {
            this.preco = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return;
        }
        try {
            String normalizedPrecoStr = precoStr.trim().replace(",", ".");
            this.preco = new BigDecimal(normalizedPrecoStr).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            this.preco = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "Produto{id=%d, nome='%s', descricao='%s', preco=%s}",
                id, nome, descricao, (preco != null ? preco.toPlainString() : "null"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return id == produto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}