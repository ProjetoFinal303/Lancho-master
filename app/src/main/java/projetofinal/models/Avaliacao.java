package projetofinal.models;

import com.google.gson.annotations.SerializedName;

public class Avaliacao {

    @SerializedName("id")
    private int id;

    @SerializedName("produto_id")
    private int produtoId;

    @SerializedName("cliente_id")
    private int clienteId;

    @SerializedName("nota")
    private int nota;

    @SerializedName("comentario")
    private String comentario;

    // Construtor para criar uma nova avaliação para envio
    public Avaliacao(int produtoId, int clienteId, int nota, String comentario) {
        this.produtoId = produtoId;
        this.clienteId = clienteId;
        this.nota = nota;
        this.comentario = comentario;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(int produtoId) {
        this.produtoId = produtoId;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}