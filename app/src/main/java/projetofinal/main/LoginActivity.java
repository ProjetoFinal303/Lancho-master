package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityLoginBinding;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ClienteDao clienteDao;

    public static final String PREFS_NAME = "UserSessionPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_ROLE = "userRole";
    public static final String KEY_USER_NOME = "userNome";
    private static final String TAG = "LoginActivity";

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

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnIrParaCadastro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastrarClienteActivity.class))
        );
    }

    private void attemptLogin() {
        String email = binding.edtEmail.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        clienteDao.buscarPorEmail(email,
                // Callback de Sucesso
                cliente -> runOnUiThread(() -> {
                    setLoading(false);
                    if (cliente != null && cliente.getSenha().equals(senha)) {
                        if ("admin@lancho.com".equalsIgnoreCase(cliente.getEmail())) {
                            Log.d(TAG, "Login como Admin bem-sucedido");
                            saveUserSession(cliente.getId(), "admin", cliente.getNome());
                            navigateToRoleSpecificActivity("admin");
                        } else {
                            Log.d(TAG, "Login do cliente bem-sucedido para: " + email);
                            saveUserSession(cliente.getId(), "cliente", cliente.getNome());
                            navigateToRoleSpecificActivity("cliente");
                        }
                    } else {
                        Log.w(TAG, "Falha no login: Email ou senha incorretos para " + email);
                        Toast.makeText(this, "Email ou senha incorretos!", Toast.LENGTH_SHORT).show();
                    }
                }),
                // Callback de Erro
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro de rede no login: ", error);
                    Toast.makeText(this, "Erro de conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
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