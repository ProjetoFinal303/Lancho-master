package projetofinal.models;

import java.io.Serializable;
import java.math.BigDecimal; // Import BigDecimal
import java.math.RoundingMode; // Import RoundingMode
import java.util.Objects;

public class CarrinhoItem implements Serializable {
    private Produto produto;
    private int quantidade;

    public CarrinhoItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo no CarrinhoItem.");
        }
        this.produto = produto;
        this.quantidade = Math.max(0, quantidade); // Garante que a quantidade não seja negativa
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo no CarrinhoItem.");
        }
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = Math.max(0, quantidade);
    }

    /**
     * Calcula o preço total para este item do carrinho (preço do produto * quantidade).
     * Retorna BigDecimal para manter a precisão.
     * @return BigDecimal representando o preço total do item.
     */
    public BigDecimal getPrecoTotalItem() {
        if (produto == null || produto.getPreco() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP); // Retorna zero se o produto ou preço for nulo
        }
        // CORREÇÃO AQUI: Usa o método multiply() do BigDecimal
        BigDecimal precoProduto = produto.getPreco();
        BigDecimal quantidadeDecimal = BigDecimal.valueOf(this.quantidade);
        return precoProduto.multiply(quantidadeDecimal).setScale(2, RoundingMode.HALF_UP);
    }

    public void incrementarQuantidade() {
        this.quantidade++;
    }

    public void decrementarQuantidade() {
        if (this.quantidade > 1) { // Só decrementa se for maior que 1, para remover use outra lógica
            this.quantidade--;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarrinhoItem that = (CarrinhoItem) o;
        // Considera igual se o ID do produto for o mesmo
        return produto != null && that.produto != null && produto.getId() == that.produto.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(produto != null ? produto.getId() : 0);
    }

    @Override
    public String toString() {
        return "CarrinhoItem{" +
                "produto=" + (produto != null ? produto.getNome() : "null") +
                ", quantidade=" + quantidade +
                ", precoTotal=" + getPrecoTotalItem().toPlainString() +
                '}';
    }
}
