package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private ClienteDao clienteDao;
    private GoogleSignInClient mGoogleSignInClient;

    public static final String PREFS_NAME = "UserSessionPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_ROLE = "userRole";
    public static final String KEY_USER_NOME = "userNome";
    private static final String TAG = "LoginActivity";

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                } else {
                    setLoading(false);
                    Toast.makeText(this, "Login com Google cancelado.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (isUserLoggedIn()) {
            navigateToRoleSpecificActivity(getLoggedInUserRole());
            return;
        }

        clienteDao = new ClienteDao(this);

        // Configura o cliente de Login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configura os cliques dos botões
        binding.btnLogin.setOnClickListener(v -> attemptManualLogin());
        binding.btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        binding.btnIrParaCadastro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastrarClienteActivity.class))
        );
    }

    private void attemptManualLogin() {
        String email = binding.edtEmail.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        clienteDao.buscarPorEmail(email,
                cliente -> runOnUiThread(() -> {
                    setLoading(false);
                    if (cliente != null && cliente.getSenha().equals(senha)) {
                        if ("admin@lancho.com".equalsIgnoreCase(cliente.getEmail())) {
                            saveUserSession(cliente.getId(), "admin", cliente.getNome());
                            navigateToRoleSpecificActivity("admin");
                        } else {
                            saveUserSession(cliente.getId(), "cliente", cliente.getNome());
                            navigateToRoleSpecificActivity("cliente");
                        }
                    } else {
                        Toast.makeText(this, "Email ou senha incorretos!", Toast.LENGTH_SHORT).show();
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro de conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void signInWithGoogle() {
        setLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            autenticarComSupabase(idToken);
        } catch (ApiException e) {
            Log.w(TAG, "Falha no login com Google. Código: " + e.getStatusCode());
            Toast.makeText(this, "Falha no login com Google.", Toast.LENGTH_SHORT).show();
            setLoading(false);
        }
    }

    private void autenticarComSupabase(String idToken) {
        String functionUrl = "https://ygsziltorjcgpjbmlptr.supabase.co/functions/v1/handle-google-signin";
        String anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlnc3ppbHRvcmpjZ3BqYm1scHRyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgyOTUzNTQsImV4cCI6MjA2Mzg3MTM1NH0.3J19gnI_qwM3nWolVdvCcNNusC3YlOTvZEjOwM6z2PU";

        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("id_token", idToken);
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(jsonPayload.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(functionUrl)
                    .header("Authorization", "Bearer " + anonKey)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Erro de rede ao validar login.", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject userJson = new JSONObject(responseBody);
                            int userId = userJson.getInt("id");
                            String userNome = userJson.getString("nome");
                            String userEmail = userJson.getString("email");

                            runOnUiThread(() -> {
                                if ("admin@lancho.com".equalsIgnoreCase(userEmail)) {
                                    saveUserSession(userId, "admin", userNome);
                                    navigateToRoleSpecificActivity("admin");
                                } else {
                                    saveUserSession(userId, "cliente", userNome);
                                    navigateToRoleSpecificActivity("cliente");
                                }
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(LoginActivity.this, "Erro ao processar dados do servidor.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Falha na autenticação com o servidor.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                setLoading(false);
                Toast.makeText(this, "Erro ao preparar requisição.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnGoogleLogin.setEnabled(!isLoading);
        binding.btnIrParaCadastro.setEnabled(!isLoading);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return !TextUtils.isEmpty(prefs.getString(KEY_USER_ROLE, null));
    }

    private String getLoggedInUserRole() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ROLE, "");
    }

    private void saveUserSession(int userId, String role, String nome) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_NOME, nome);
        editor.apply();
    }

    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(LoginActivity.this, MainAdminActivity.class);
                break;
            case "cozinha":
                intent = new Intent(LoginActivity.this, MainCozinhaActivity.class);
                break;
            case "cliente":
            default:
                intent = new Intent(LoginActivity.this, MainClienteActivity.class);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}