package projetofinal.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "Cliente")
public class Cliente {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "nome")
    private String nome;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "contato")
    private String contato;

    @ColumnInfo(name = "senha")
    private String senha;

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
