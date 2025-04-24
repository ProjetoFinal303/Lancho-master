package projetofinal.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "Estoque")
public class Estoque {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "id_produto")
    private int produtoId;

    @ColumnInfo(name = "quantidade")
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
