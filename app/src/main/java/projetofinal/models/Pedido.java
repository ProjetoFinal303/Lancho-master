package projetofinal.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "Pedido")
public class Pedido {

    @PrimaryKey(autoGenerate = true) 
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "id_cliente")
    private int clienteId;

    @ColumnInfo(name = "descricao")
    private String descricao;

    @ColumnInfo(name = "valor")
    private double valor;

    // Construtor vazio necessário para o SQLite
    public Pedido() {}

    // Construtor completo
    public Pedido(int id, int clienteId, String descricao, double valor) {
        this.id = id;
        this.clienteId = clienteId;
        this.descricao = descricao;
        this.valor = valor;
    }

    // Métodos Getter
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }

    // Métodos Setter
    public void setId(int id) { this.id = id; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }

    // Método para facilitar debug
    @Override
    public String toString() {
        return String.format("Pedido { ID: %d, Cliente ID: %d, Descrição: '%s', Valor: %.2f }",
                id, clienteId, descricao, valor);
    }
}
