package projetofinal.dao;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Pedido;

public class PedidoDao {
    private static final String TABELA = "Pedido";

    public PedidoDao(Context context) {}

    private Pedido parsePedido(JSONObject obj) throws Exception {
        return new Pedido(
                obj.getInt("id"), obj.getInt("id_cliente"),
                obj.getString("descricao"), obj.getDouble("valor"),
                obj.getString("data"), obj.getString("status")
        );
    }

    public void buscarPorId(int id, Consumer<Pedido> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?id=eq." + id;
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                onSuccess.accept(array.length() > 0 ? parsePedido(array.getJSONObject(0)) : null);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void listarTodos(Consumer<List<Pedido>> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABELA + "?select=*", response -> {
            try {
                JSONArray array = new JSONArray(response);
                List<Pedido> lista = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    lista.add(parsePedido(array.getJSONObject(i)));
                }
                onSuccess.accept(lista);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void buscarPedidosPorClienteId(int clienteId, Consumer<List<Pedido>> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?id_cliente=eq." + clienteId;
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                List<Pedido> lista = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    lista.add(parsePedido(array.getJSONObject(i)));
                }
                onSuccess.accept(lista);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void inserir(Pedido pedido, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("id_cliente", pedido.getClienteId());
            json.put("descricao", pedido.getDescricao());
            json.put("valor", pedido.getValor());
            json.put("data", pedido.getData());
            json.put("status", pedido.getStatus());
            SupabaseDatabaseClient.insert(TABELA, json.toString(), onSuccess, onError);
        } catch (Exception e) { onError.accept(e); }
    }

    public void atualizarStatus(int pedidoId, String novoStatus, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("status", novoStatus);
            String urlPath = TABELA + "?id=eq." + pedidoId;
            SupabaseDatabaseClient.patch(urlPath, json.toString(), onSuccess, onError);
        } catch (Exception e) { onError.accept(e); }
    }

    public void excluir(int id, Consumer<String> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?id=eq." + id;
        SupabaseDatabaseClient.delete(urlPath, onSuccess, onError);
    }
}