package projetofinal.dao;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Estoque;

public class EstoqueDao {
    private static final String TABELA = "Estoque";

    public EstoqueDao(Context context) {}

    public void buscarPorProdutoId(int produtoId, Consumer<Estoque> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?id_produto=eq." + produtoId;
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                if (array.length() > 0) {
                    JSONObject obj = array.getJSONObject(0);
                    onSuccess.accept(new Estoque(
                            obj.getInt("id"), obj.getInt("id_produto"), obj.getInt("quantidade")
                    ));
                } else {
                    onSuccess.accept(null);
                }
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void inserirOuAtualizar(Estoque estoque, Consumer<String> onSuccess, Consumer<Exception> onError) {
        buscarPorProdutoId(estoque.getProdutoId(),
                estoqueExistente -> {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("id_produto", estoque.getProdutoId());
                        json.put("quantidade", estoque.getQuantidade());
                        if (estoqueExistente != null) {
                            String urlPath = TABELA + "?id=eq." + estoqueExistente.getId();
                            SupabaseDatabaseClient.patch(urlPath, json.toString(), onSuccess, onError);
                        } else {
                            SupabaseDatabaseClient.insert(TABELA, json.toString(), onSuccess, onError);
                        }
                    } catch (Exception e) { onError.accept(e); }
                },
                onError
        );
    }

    public void listarTodosComNomeProduto(Consumer<List<Estoque>> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?select=id,id_produto,quantidade,Produto(nome)";
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                List<Estoque> lista = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Estoque e = new Estoque(
                            obj.getInt("id"),
                            obj.getInt("id_produto"),
                            obj.getInt("quantidade")
                    );
                    JSONObject produtoObj = obj.getJSONObject("Produto");
                    e.setNomeProduto(produtoObj.getString("nome"));
                    lista.add(e);
                }
                onSuccess.accept(lista);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void excluirPorProdutoId(int produtoId, Consumer<String> onSuccess, Consumer<Exception> onError) {
        buscarPorProdutoId(produtoId,
                estoque -> {
                    if (estoque != null) {
                        String urlPath = TABELA + "?id=eq." + estoque.getId();
                        SupabaseDatabaseClient.delete(urlPath, onSuccess, onError);
                    } else {
                        onSuccess.accept("Nenhum estoque para excluir.");
                    }
                },
                onError
        );
    }
}