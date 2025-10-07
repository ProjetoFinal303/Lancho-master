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
    private static final String TABLE_NAME = "Produto";

    public ProdutoDao(Context context) {
        // O construtor está correto como está.
    }

    private Produto parseProdutoComEstoque(JSONObject obj) {
        Produto p = new Produto();
        try {
            p.setId(obj.getInt("id"));
            p.setNome(obj.getString("nome"));
            p.setDescricao(obj.optString("descricao", ""));
            p.setPreco(new BigDecimal(obj.getString("preco")));
            p.setImageUrl(obj.optString("image_url", ""));
            p.setStripePriceId(obj.optString("stripe_price_id", ""));
            p.setCategoria(obj.optString("categoria", ""));
            p.setMediaAvaliacoes(obj.optDouble("media_avaliacoes", 0.0));
            p.setTotalAvaliacoes(obj.optInt("total_avaliacoes", 0));

            // ============================ CORREÇÃO DEFINITIVA ============================
            // Esta nova lógica verifica se "Estoque" é um objeto único (para relação 1-para-1)
            // ou um array (caso a restrição mude no futuro). Isso torna o código robusto.
            JSONObject estoqueObj = obj.optJSONObject("Estoque");
            if (estoqueObj != null) {
                // Se for um objeto único, pega a quantidade diretamente.
                p.setQuantidadeEstoque(estoqueObj.optInt("quantidade", 0));
            } else {
                // Se não for um objeto, tenta ler como um array.
                JSONArray estoqueArray = obj.optJSONArray("Estoque");
                if (estoqueArray != null && estoqueArray.length() > 0) {
                    p.setQuantidadeEstoque(estoqueArray.getJSONObject(0).optInt("quantidade", 0));
                } else {
                    // Se não encontrar nem objeto nem array, o estoque é 0.
                    p.setQuantidadeEstoque(0);
                }
            }
            // ===========================================================================

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }

    public void listarTodosComEstoque(Consumer<List<Produto>> onSuccess, Consumer<Exception> onError) {
        String selectQuery = "*,Estoque(quantidade)";

        SupabaseDatabaseClient.get(TABLE_NAME + "?select=" + selectQuery,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        List<Produto> produtos = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Produto p = parseProdutoComEstoque(jsonArray.getJSONObject(i));
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

    // Mantém o método original para não quebrar outras partes do app
    public void listarTodos(Consumer<List<Produto>> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABLE_NAME,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        List<Produto> produtos = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Produto p = parseProdutoComEstoque(jsonArray.getJSONObject(i)); // Usa o parser novo aqui também
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
        String selectQuery = "*,Estoque(quantidade)";
        SupabaseDatabaseClient.get(TABLE_NAME + "?id=eq." + id + "&select=" + selectQuery,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() > 0) {
                            onSuccess.accept(parseProdutoComEstoque(jsonArray.getJSONObject(0)));
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