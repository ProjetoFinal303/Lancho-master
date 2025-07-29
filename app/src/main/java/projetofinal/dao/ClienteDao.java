package projetofinal.dao;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import projetofinal.database.SupabaseDatabaseClient;
import projetofinal.models.Cliente;

public class ClienteDao {
    private static final String TABELA = "Cliente";

    public ClienteDao(Context context) {}

    private Cliente parseCliente(JSONObject obj) throws Exception {
        return new Cliente(
                obj.getInt("id"), obj.getString("nome"), obj.getString("email"),
                obj.getString("contato"), obj.optString("senha", "") // senha pode ser nula
        );
    }

    public void buscarPorId(int id, Consumer<Cliente> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?id=eq." + id;
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                onSuccess.accept(array.length() > 0 ? parseCliente(array.getJSONObject(0)) : null);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void buscarPorEmail(String email, Consumer<Cliente> onSuccess, Consumer<Exception> onError) {
        String urlPath = TABELA + "?email=eq." + email;
        SupabaseDatabaseClient.get(urlPath, response -> {
            try {
                JSONArray array = new JSONArray(response);
                onSuccess.accept(array.length() > 0 ? parseCliente(array.getJSONObject(0)) : null);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void getAllClientes(Consumer<List<Cliente>> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.get(TABELA + "?select=*", response -> {
            try {
                JSONArray array = new JSONArray(response);
                List<Cliente> lista = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    lista.add(parseCliente(array.getJSONObject(i)));
                }
                onSuccess.accept(lista);
            } catch (Exception e) { onError.accept(e); }
        }, onError);
    }

    public void inserir(Cliente cliente, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("nome", cliente.getNome());
            json.put("email", cliente.getEmail());
            json.put("contato", cliente.getContato());
            json.put("senha", cliente.getSenha());
            SupabaseDatabaseClient.insert(TABELA, json, onSuccess, onError);
        } catch (Exception e) { onError.accept(e); }
    }

    public void atualizar(Cliente cliente, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("nome", cliente.getNome());
            json.put("email", cliente.getEmail());
            json.put("contato", cliente.getContato());
            if (cliente.getSenha() != null && !cliente.getSenha().isEmpty()) {
                json.put("senha", cliente.getSenha());
            }
            SupabaseDatabaseClient.update(TABELA, cliente.getId(), json, onSuccess, onError);
        } catch (Exception e) { onError.accept(e); }
    }

    public void excluir(int id, Consumer<String> onSuccess, Consumer<Exception> onError) {
        SupabaseDatabaseClient.delete(TABELA, id, onSuccess, onError);
    }
}