package projetofinal.models;

public class Cliente {

    private int id;
    private String nome;
    private String email;
    private String contato;
    private String senha;
    private String avatarUrl;
    private String role; // <-- CAMPO ADICIONADO

    // Construtor vazio (necessário)
    public Cliente() {}

    // Construtor para a tela de cadastro
    public Cliente(String nome, String email, String contato, String senha) {
        this.nome = nome;
        this.email = email;
        this.contato = contato;
        this.senha = senha;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // GETTER E SETTER PARA ROLE ADICIONADOS
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}