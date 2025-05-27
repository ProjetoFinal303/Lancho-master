package projetofinal.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Pedido {

    private int id;
    private int clienteId;
    private String descricao;
    private double valor;
    private String data; //YYYY-MM-DD HH:MM:SS
    private String status; // Ex: Pendente, Em preparo, Concluído, Cancelado

    public Pedido() {}

    public Pedido(int clienteId, String descricao, double valor) {
        this.clienteId = clienteId;
        this.descricao = descricao;
        this.valor = valor;
        this.data = getCurrentDateTime();
        this.status = "Pendente";
    }

    // Full constructor
    public Pedido(int id, int clienteId, String descricao, double valor, String data, String status) {
        this.id = id;
        this.clienteId = clienteId;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.status = status;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // Getters
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public String getData() { return data; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }
    public void setData(String data) { this.data = data; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "Pedido { ID: %d, Cliente ID: %d, Descrição: '%s', Valor: %.2f, Data: '%s', Status: '%s' }",
                id, clienteId, descricao, valor, data, status);
    }
}