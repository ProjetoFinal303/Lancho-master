package projetofinal.database;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseStorageClient {

    private static final String SUPABASE_URL = "https://ygsziltorjcgpjbmlptr.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlnc3ppbHRvcmpjZ3BqYm1scHRyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgyOTUzNTQsImV4cCI6MjA2Mzg3MTM1NH0.3J19gnI_qwM3nWolVdvCcNNusC3YlOTvZEjOwM6z2PU";
    private static final String BUCKET_NAME = "produtos";
    private static final OkHttpClient client = new OkHttpClient();

    public static void uploadFile(Context context, Uri fileUri, Consumer<String> onSuccess, Consumer<Exception> onError) {
        try {
            // Converte o Uri em um array de bytes
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            byte[] fileBytes = getBytes(inputStream);
            String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(fileUri));
            String fileName = "public/" + System.currentTimeMillis() + "." + fileExtension;

            // Cria o corpo da requisição com os bytes do arquivo
            RequestBody body = RequestBody.create(fileBytes, MediaType.parse(context.getContentResolver().getType(fileUri)));

            // Monta a URL do endpoint de upload do Supabase Storage
            String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onError.accept(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Se o upload deu certo, constrói a URL pública final
                        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
                        onSuccess.accept(publicUrl);
                    } else {
                        onError.accept(new IOException("Erro no upload: " + response.code() + " " + response.body().string()));
                    }
                }
            });

        } catch (IOException e) {
            onError.accept(e);
        }
    }

    // Métdo auxiliar para converter InputStream para byte[]
    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}