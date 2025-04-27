package projetofinal.models;

public class Pedido {

    private int id;
    private int clienteId;
    private String descricao;
    private double valor;

    public Pedido() {}

    public Pedido(int id, int clienteId, String descricao, double valor) {
        this.id = id;
        this.clienteId = clienteId;
        this.descricao = descricao;
        this.valor = valor;
    }

    // Métodos Getter e Setter
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }

    public void setId(int id) { this.id = id; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }

    @Override
    public String toString() {
        return String.format("Pedido { ID: %d, Cliente ID: %d, Descrição: '%s', Valor: %.2f }", id, clienteId, descricao, valor);
    }
}
