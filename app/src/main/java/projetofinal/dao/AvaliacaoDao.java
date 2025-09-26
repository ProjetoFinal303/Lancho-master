package projetofinal.dao;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Avaliacao;

public class AvaliacaoDao {
    private static final String TABLE_NAME = "avaliacoes"; // Nome da sua tabela no Supabase
    private final OkHttpClient client;
    private final Gson gson;
    private static final String TAG = "AvaliacaoDao";

    public AvaliacaoDao(Context context) {
        this.client = new OkHttpClient();
        this.gson = new GsonBuilder().create();
    }

    // Método para inserir uma nova avaliação
    public void inserir(Avaliacao avaliacao, Runnable onSuccess, Consumer<String> onError) {
        Request request = SupabaseDatabaseClient.createInsertRequest(TABLE_NAME, avaliacao);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Falha ao inserir avaliação", e);
                onError.accept("Erro de rede: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    onSuccess.run();
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Corpo vazio";
                    Log.e(TAG, "Erro ao inserir avaliação: " + response.code() + " - " + errorBody);
                    onError.accept("Falha ao inserir: " + errorBody);
                }
            }
        });
    }

    // Método para buscar avaliações por ID do produto
    public void getAvaliacoesPorProduto(int produtoId, Consumer<List<Avaliacao>> onSuccess, Consumer<String> onError) {
        String filter = "produto_id=eq." + produtoId;
        Request request = SupabaseDatabaseClient.createSelectRequest(TABLE_NAME, filter);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Falha ao buscar avaliações", e);
                onError.accept(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<Avaliacao>>(){}.getType();
                    List<Avaliacao> result = gson.fromJson(responseBody, listType);
                    onSuccess.accept(result);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Corpo vazio";
                    Log.e(TAG, "Erro ao buscar avaliações: " + response.code() + " - " + errorBody);
                    onError.accept(errorBody);
                }
            }
        });
    }
}