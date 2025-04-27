package projetofinal.models;

public class Estoque {

    private int id;
    private int produtoId;
    private int quantidade;

    public Estoque() {}

    public Estoque(int id, int produtoId, int quantidade) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    // Getters e Setters
    public int getId() { return id; }
    public int getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }

    public void setId(int id) { this.id = id; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}
