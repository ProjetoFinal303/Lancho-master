package projetofinal.dao;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Produto;

public class ProdutoDao {
    private static final String TABELA = "Produto";

    public ProdutoDao(Context context) {}

    private Produto parseProduto(JSONObject obj) throws Exception {
        Produto p = new Produto();
        // O ID do produto no app continua sendo o ID do Preço do Stripe
        p.setId(obj.getString("stripe_price_id"));
        p.setNome(obj.getString("nome"));
        p.setDescricao(obj.optString("descricao", ""));
        p.setImageUrl(obj.optString("image_url", ""));
        p.setPreco(new BigDecimal(obj.getDouble("preco")));
        return p;
    }

    public void listarTodos(Consumer<List<Produto>> onSuccess, Consumer<Exception> onError) {
        // Ordena por nome para uma exibição consistente
        String urlPath = TABELA + "?select=*&order=nome.asc";
        SupabaseDatabaseClient.get(urlPath, response -> {
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
}