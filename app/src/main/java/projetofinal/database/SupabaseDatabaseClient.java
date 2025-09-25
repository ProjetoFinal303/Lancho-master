package projetofinal.database;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class SupabaseDatabaseClient {

    private static final String SUPABASE_URL = "https://ygsziltorjcgpjbmlptr.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlnc3ppbHRvcmpjZ3BqYm1scHRyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgyOTUzNTQsImV4cCI6MjA2Mzg3MTM1NH0.3J19gnI_qwM3nWolVdvCcNNusC3YlOTvZEjOwM6z2PU";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    // =================================================================
    // MÉTODOS ORIGINAIS (MANTIDOS PARA COMPATIBILIDADE)
    // =================================================================

    public static void get(String urlPath, Consumer<String> onSuccess, Consumer<Exception> onError) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + urlPath)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .get()
                .build();
        client.newCall(request).enqueue(new SimpleCallback(onSuccess, onError));
    }

    public static void insert(String table, String jsonData, Consumer<String> onSuccess, Consumer<Exception> onError) {
        RequestBody body = RequestBody.create(jsonData, JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();
        client.newCall(request).enqueue(new SimpleCallback(onSuccess, onError));
    }

    public static void patch(String urlPath, String jsonData, Consumer<String> onSuccess, Consumer<Exception> onError) {
        RequestBody body = RequestBody.create(jsonData, JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + urlPath)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .patch(body)
                .build();
        client.newCall(request).enqueue(new SimpleCallback(onSuccess, onError));
    }

    public static void delete(String urlPath, Consumer<String> onSuccess, Consumer<Exception> onError) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + urlPath)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .delete()
                .build();
        client.newCall(request).enqueue(new SimpleCallback(onSuccess, onError));
    }

    public static void resetPasswordForEmail(String email, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/recover")
                    .addHeader("apikey", SUPABASE_KEY)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new SimpleCallback(onSuccess, onError));
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private static class SimpleCallback implements Callback {
        private final Consumer<String> onSuccess;
        private final Consumer<Exception> onError;
        SimpleCallback(Consumer<String> onSuccess, Consumer<Exception> onError) {
            this.onSuccess = onSuccess;
            this.onError = onError;
        }
        @Override
        public void onFailure(Call call, IOException e) { onError.accept(e); }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try (Response r = response) {
                String responseBody = r.body().string();
                if (r.isSuccessful()) {
                    onSuccess.accept(responseBody);
                } else {
                    onError.accept(new IOException("Erro: " + r.code() + " " + responseBody));
                }
            }
        }
    }

    // =================================================================
    // NOVOS MÉTODOS (ADICIONADOS PARA O NOVO PEDIDODAO)
    // =================================================================

    private static Request.Builder createBaseRequestBuilder(String tableName) {
        return new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + tableName)
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY);
    }

    public static Request createSelectRequest(String tableName, String filters) {
        String url = SUPABASE_URL + "/rest/v1/" + tableName + (filters != null ? "?" + filters : "");
        return new Request.Builder()
                .url(url)
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .header("Accept", "application/json")
                .get()
                .build();
    }

    public static Request createInsertRequest(String tableName, Object object) {
        String json = gson.toJson(object);
        RequestBody body = RequestBody.create(json, JSON);
        return createBaseRequestBuilder(tableName)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .post(body)
                .build();
    }

    public static Request createUpdateRequest(String tableName, String filter, Map<String, Object> updates) {
        String json = gson.toJson(updates);
        RequestBody body = RequestBody.create(json, JSON);
        return createBaseRequestBuilder(tableName)
                .url(SUPABASE_URL + "/rest/v1/" + tableName + "?" + filter)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .patch(body)
                .build();
    }

    public static Request createDeleteRequest(String tableName, String filter) {
        return createBaseRequestBuilder(tableName)
                .url(SUPABASE_URL + "/rest/v1/" + tableName + "?" + filter)
                .delete()
                .build();
    }
}