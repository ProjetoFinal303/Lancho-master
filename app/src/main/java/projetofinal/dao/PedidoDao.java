package projetofinal.dao;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Pedido;

public class PedidoDao {
    private static final String TABLE_NAME = "Pedido";
    private final OkHttpClient client;
    private final Gson gson;
    private static final String TAG = "PedidoDao";

    public PedidoDao(Context context) {
        this.client = new OkHttpClient();
        this.gson = new GsonBuilder().create();
    }

    public void getPedidosPorStatus(List<String> statusList, Consumer<List<Pedido>> onSuccess, Consumer<String> onError) {
        if (statusList == null || statusList.isEmpty()) {
            onSuccess.accept(new ArrayList<>());
            return;
        }

        StringBuilder filterBuilder = new StringBuilder("status=in.(");
        for (int i = 0; i < statusList.size(); i++) {
            filterBuilder.append("\"").append(statusList.get(i)).append("\"");
            if (i < statusList.size() - 1) {
                filterBuilder.append(",");
            }
        }
        filterBuilder.append(")");

        Request request = SupabaseDatabaseClient.createSelectRequest(TABLE_NAME, filterBuilder.toString());
        executeRequestForList(request, getListType(), onSuccess, onError);
    }

    public void updateStatus(int pedidoId, String novoStatus, Runnable onSuccess, Consumer<String> onError) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", novoStatus);

        Request request = SupabaseDatabaseClient.createUpdateRequest(TABLE_NAME, "id=eq." + pedidoId, updates);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Falha ao atualizar status do pedido #" + pedidoId, e);
                onError.accept("Erro de rede: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    onSuccess.run();
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Corpo da resposta vazio";
                    Log.e(TAG, "Erro ao atualizar status do pedido #" + pedidoId + ": " + response.code() + " - " + errorBody);
                    onError.accept("Falha na atualização: " + errorBody);
                }
            }
        });
    }

    // =================================================================
    // MÉTODOS ANTIGOS RESTAURADOS (PARA COMPATIBILIDADE)
    // =================================================================

    public void listarTodos(Consumer<List<Pedido>> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABLE_NAME,
                response -> {
                    List<Pedido> pedidos = gson.fromJson(response, new TypeToken<List<Pedido>>(){}.getType());
                    onSuccess.accept(pedidos);
                },
                onError
        );
    }

    public void buscarPorId(int id, Consumer<Pedido> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABLE_NAME + "?id=eq." + id,
                response -> {
                    List<Pedido> pedidos = gson.fromJson(response, new TypeToken<List<Pedido>>(){}.getType());
                    if (pedidos != null && !pedidos.isEmpty()) {
                        onSuccess.accept(pedidos.get(0));
                    } else {
                        onSuccess.accept(null);
                    }
                },
                onError
        );
    }

    public void buscarPedidosPorClienteId(int clienteId, Consumer<List<Pedido>> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABLE_NAME + "?id_cliente=eq." + clienteId,
                response -> {
                    List<Pedido> pedidos = gson.fromJson(response, new TypeToken<List<Pedido>>(){}.getType());
                    onSuccess.accept(pedidos);
                },
                onError
        );
    }

    public void excluir(int id, Consumer<String> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.delete(TABLE_NAME + "?id=eq." + id, onSuccess, onError);
    }

    // =================================================================
    // HELPERS INTERNOS
    // =================================================================

    private <T> void executeRequestForList(Request request, Type type, Consumer<T> onSuccess, Consumer<String> onError) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Falha na requisição", e);
                onError.accept(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    T result = gson.fromJson(responseBody, type);
                    onSuccess.accept(result);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Corpo da resposta vazio";
                    Log.e(TAG, "Erro na requisição: " + response.code() + " - " + errorBody);
                    onError.accept(errorBody);
                }
            }
        });
    }

    private Type getListType() {
        return new TypeToken<List<Pedido>>(){}.getType();
    }
}