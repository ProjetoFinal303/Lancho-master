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
        // O construtor está correto, pois o SupabaseClient não depende do context.
    }

    private Produto parseProdutoComEstoque(JSONObject obj) {
        if (obj == null) return null;
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

            // ============================ CORREÇÃO DA LEITURA DO ESTOQUE ============================
            // Esta nova lógica verifica se "Estoque" é um objeto único (para relação 1-para-1)
            // ou um array. Isso torna o código robusto e resolve o problema do estoque zerado.
            int quantidade = 0;
            if (obj.has("Estoque") && !obj.isNull("Estoque")) {
                // Tenta ler como um Array primeiro (padrão do Supabase)
                JSONArray estoqueArray = obj.optJSONArray("Estoque");
                if (estoqueArray != null && estoqueArray.length() > 0) {
                    quantidade = estoqueArray.getJSONObject(0).optInt("quantidade", 0);
                } else {
                    // Se não for um array, tenta ler como um Objeto (caso de relação 1-para-1 direta)
                    JSONObject estoqueObj = obj.optJSONObject("Estoque");
                    if (estoqueObj != null) {
                        quantidade = estoqueObj.optInt("quantidade", 0);
                    }
                }
            }
            p.setQuantidadeEstoque(quantidade);
            // =======================================================================================

        } catch (Exception e) {
            e.printStackTrace();
            return null; // Retorna null se houver erro para não adicionar um produto malformado à lista
        }
        return p;
    }

    public void listarTodosComEstoque(Consumer<List<Produto>> onSuccess, Consumer<Exception> onError) {
        // A query que busca o produto e seu estoque relacionado
        String selectQuery = "*,Estoque(quantidade)";

        SupabaseDatabaseClient.get(TABLE_NAME + "?select=" + selectQuery,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        List<Produto> produtos = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Produto p = parseProdutoComEstoque(jsonArray.getJSONObject(i));
                            if (p != null) { // Adiciona à lista apenas se o parsing foi bem-sucedido
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

    // Mantém este método para compatibilidade, mas agora ele também busca o estoque.
    public void listarTodos(Consumer<List<Produto>> onSuccess, Consumer<Exception> onError) {
        listarTodosComEstoque(onSuccess, onError);
    }

    public void inserir(Produto produto, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("nome", produto.getNome());
            json.put("descricao", produto.getDescricao());
            json.put("preco", produto.getPreco().doubleValue());
            json.put("image_url", produto.getImageUrl());
            json.put("stripe_price_id", produto.getStripePriceId());
            json.put("categoria", produto.getCategoria());

            SupabaseDatabaseClient.insert(TABLE_NAME, json.toString(), onSuccess, onError);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    public void atualizar(Produto produto, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("nome", produto.getNome());
            json.put("descricao", produto.getDescricao());
            json.put("preco", produto.getPreco().doubleValue());
            if (produto.getImageUrl() != null && !produto.getImageUrl().isEmpty()) {
                json.put("image_url", produto.getImageUrl());
            }

            String urlPath = TABLE_NAME + "?id=eq." + produto.getId();
            SupabaseDatabaseClient.patch(urlPath, json.toString(), onSuccess, onError);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    public void excluir(int id, Consumer<String> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.delete(TABLE_NAME + "?id=eq." + id, onSuccess, onError);
    }
}