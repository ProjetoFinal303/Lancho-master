package projetofinal.models;

public class Estoque {

    private int id;
    private int produtoId;
    private int quantidade;
    private String nomeProduto;
    public Estoque() {}

    public Estoque(int id, int produtoId, int quantidade) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Estoque(int produtoId, int quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }


    // Getters
    public int getId() { return id; }
    public int getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }
    public String getNomeProduto() { return nomeProduto; }


    // Setters
    public void setId(int id) { this.id = id; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; } // Setter for transient field
}