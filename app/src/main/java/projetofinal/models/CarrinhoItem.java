package projetofinal.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CarrinhoItem implements Serializable {
    private Produto produto;
    private int quantidade;

    // Campo novo para enviar para a função de checkout
    private String priceId;

    public CarrinhoItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo no CarrinhoItem.");
        }
        this.produto = produto;
        this.priceId = produto.getId(); // Guarda o Price ID
        this.quantidade = Math.max(0, quantidade);
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo.");
        }
        this.produto = produto;
        this.priceId = produto.getId();
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = Math.max(0, quantidade);
    }

    public BigDecimal getPrecoTotalItem() {
        if (produto == null || produto.getPreco() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal precoProduto = produto.getPreco();
        BigDecimal quantidadeDecimal = BigDecimal.valueOf(this.quantidade);
        return precoProduto.multiply(quantidadeDecimal).setScale(2, RoundingMode.HALF_UP);
    }

    public void incrementarQuantidade() {
        this.quantidade++;
    }

    public void decrementarQuantidade() {
        if (this.quantidade > 1) {
            this.quantidade--;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarrinhoItem that = (CarrinhoItem) o;
        return Objects.equals(priceId, that.priceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priceId);
    }
}