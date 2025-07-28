package projetofinal.models;

public class Cliente {

    private int id;
    private String nome;
    private String email;
    private String contato;
    private String senha;

    // Construtor vazio que estava faltando
    public Cliente() {}

    public Cliente(int id, String nome, String email, String contato, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.contato = contato;
        this.senha = senha;
    }

    public Cliente(String nome, String email, String contato, String senha) {
        this.nome = nome;
        this.email = email;
        this.contato = contato;
        this.senha = senha;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getContato() { return contato; }
    public String getSenha() { return senha; }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setContato(String contato) { this.contato = contato; }
    public void setSenha(String senha) { this.senha = senha; }
}