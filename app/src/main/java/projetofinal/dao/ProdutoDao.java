package projetofinal.dao;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Produto;

public class ProdutoDao {
    private static final String TABELA = "Produto";

    public ProdutoDao(Context context) {}

    private Produto parseProduto(JSONObject obj) throws Exception {
        return new Produto(
                obj.getInt("id"), obj.getString("nome"),
                obj.optString("descricao", ""), obj.getDouble("preco")
        );
    }

    public void buscarPorId(int id, Consumer<Produto> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?id=eq." + id;
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                onSuccess.accept(array.length() > 0 ? parseProduto(array.getJSONObject(0)) : null);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void buscarPorNome(String nome, Consumer<Produto> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?nome=eq." + nome;
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                onSuccess.accept(array.length() > 0 ? parseProduto(array.getJSONObject(0)) : null);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void listarTodos(Consumer<List<Produto>> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABELA + "?select=*", response -> {
            try {
                JSONArray array = new JSONArray(response);
                List<Produto> lista = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    lista.add(parseProduto(array.getJSONObject(i)));
                }
                onSuccess.accept(lista);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void inserir(Produto produto, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("nome", produto.getNome());
            json.put("descricao", produto.getDescricao());
            json.put("preco", produto.getPreco());
            SupabaseDatabaseClient.insert(TABELA, json, onSuccess, onError);
        } catch (Exception e) { onError.accept(e); }
    }

    public void atualizar(Produto produto, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("nome", produto.getNome());
            json.put("descricao", produto.getDescricao());
            json.put("preco", produto.getPreco());
            SupabaseDatabaseClient.update(TABELA, produto.getId(), json, onSuccess, onError);
        } catch (Exception e) { onError.accept(e); }
    }

    public void excluir(int id, Consumer<String> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.delete(TABELA, id, onSuccess, onError);
    }
}