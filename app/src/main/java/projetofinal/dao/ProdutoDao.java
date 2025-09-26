package projetofinal.dao;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Produto;

public class ProdutoDao {
    private static final String TABLE_NAME = "Produto";
    private final Gson gson = new Gson();

    public ProdutoDao(Context context) {
        // Construtor
    }

    private Produto parseProduto(JSONObject obj) {
        // Usa o construtor vazio que adicionamos de volta
        Produto p = new Produto();
        try {
            // CORREÇÃO AQUI: Lê o 'id' numérico corretamente
            p.setId(obj.getInt("id"));
            p.setNome(obj.getString("nome"));
            p.setDescricao(obj.optString("descricao", ""));
            p.setPreco(new BigDecimal(obj.getString("preco")));
            p.setImageUrl(obj.optString("image_url", ""));
            p.setStripePriceId(obj.optString("stripe_price_id", ""));
            p.setCategoria(obj.optString("categoria", ""));
            // Adiciona os novos campos de avaliação
            p.setMediaAvaliacoes(obj.optDouble("media_avaliacoes", 0.0));
            p.setTotalAvaliacoes(obj.optInt("total_avaliacoes", 0));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }

    public void listarTodos(Consumer<List<Produto>> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABLE_NAME,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        List<Produto> produtos = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Produto p = parseProduto(jsonArray.getJSONObject(i));
                            if (p != null) {
                                produtos.add(p);
                            }
                        }
                        onSuccess.accept(produtos);
                    } catch (Exception e) {
                        onError.accept(e);
                    }
                },
                onError
        );
    }

    public void buscarPorId(int id, Consumer<Produto> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABLE_NAME + "?id=eq." + id,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() > 0) {
                            onSuccess.accept(parseProduto(jsonArray.getJSONObject(0)));
                        } else {
                            onSuccess.accept(null);
                        }
                    } catch (Exception e) {
                        onError.accept(e);
                    }
                },
                onError
        );
    }

    public void excluir(int id, Consumer<String> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.delete(TABLE_NAME + "?id=eq." + id, onSuccess, onError);
    }
}