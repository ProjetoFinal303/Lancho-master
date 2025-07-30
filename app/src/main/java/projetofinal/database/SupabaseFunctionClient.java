package projetofinal.database;

import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.io.IOException;
import java.util.function.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseFunctionClient {

    private static final String SUPABASE_URL = "https://ygsziltorjcgpjbmlptr.supabase.co";
    // Usamos a chave ANON, pois a segurança é controlada pelo acesso ao painel admin
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlnc3ppbHRvcmpjZ3BqYm1scHRyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgyOTUzNTQsImV4cCI6MjA2Mzg3MTM1NH0.3J19gnI_qwM3nWolVdvCcNNusC3YlOTvZEjOwM6z2PU";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void invoke(String functionName, JSONObject payload, Consumer<String> onSuccess, Consumer<Exception> onError) {
        RequestBody body = RequestBody.create(payload.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/functions/v1/" + functionName)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY) // JWT e Anon Key são a mesma neste caso
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                onError.accept(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response r = response) {
                    String responseBody = r.body().string();
                    if (r.isSuccessful()) {
                        onSuccess.accept(responseBody);
                    } else {
                        onError.accept(new IOException("Erro da Função: " + r.code() + " - " + responseBody));
                    }
                }
            }
        });
    }
}