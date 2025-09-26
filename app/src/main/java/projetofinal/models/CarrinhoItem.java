package projetofinal.models;

import java.math.BigDecimal;

public class CarrinhoItem {
    private Produto produto;
    private int quantidade;
    private String priceId;

    // Construtor original que o app usa, agora com a lógica correta
    public CarrinhoItem(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        // A correção importante está aqui: usar o stripe_price_id
        this.priceId = produto.getStripePriceId();
    }

    // Mantendo o construtor de um argumento por segurança
    public CarrinhoItem(Produto produto) {
        this.produto = produto;
        this.quantidade = 1;
        this.priceId = produto.getStripePriceId();
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
        this.priceId = produto.getStripePriceId();
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public void incrementarQuantidade() {
        this.quantidade++;
    }

    public void decrementarQuantidade() {
        if (this.quantidade > 0) {
            this.quantidade--;
        }
    }

    // Nome do método corrigido de volta para getPrecoTotalItem()
    public BigDecimal getPrecoTotalItem() {
        if (produto.getPreco() == null) {
            return BigDecimal.ZERO;
        }
        return produto.getPreco().multiply(new BigDecimal(quantidade));
    }
}