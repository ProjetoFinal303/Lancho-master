package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import se for usar Toolbar aqui

import com.example.projetofinal.R; // Garanta que R seja importado corretamente
import com.example.projetofinal.databinding.ActivityLoginBinding;
import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ClienteDao clienteDao;
    private ExecutorService executorService;

    // Constantes para SharedPreferences (públicas para serem acessadas por outras activities)
    public static final String PREFS_NAME = "UserSessionPrefs"; // Nome do arquivo de preferências
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_ROLE = "userRole"; // cliente, admin, cozinha
    public static final String KEY_USER_NOME = "userNome";


    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Verificar se já existe uma sessão ativa
        if (isUserLoggedIn()) {
            navigateToRoleSpecificActivity(getLoggedInUserRole());
            return; // Pula o resto do onCreate se já estiver logado
        }


        clienteDao = new ClienteDao(this);
        executorService = Executors.newSingleThreadExecutor();

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.btnIrParaCadastro.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CadastrarClienteActivity.class));
        });
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Considera logado se tiver um USER_ROLE salvo e não vazio
        return !TextUtils.isEmpty(prefs.getString(KEY_USER_ROLE, null));
    }

    private String getLoggedInUserRole() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ROLE, "");
    }


    private void attemptLogin() {
        String emailOuNome = binding.edtEmail.getText().toString().trim(); // Campo pode ser email ou nome de usuário
        String senha = binding.edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(emailOuNome) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, getString(R.string.login_fields_required), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarLogin.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnIrParaCadastro.setEnabled(false);


        // Login ADM (hardcoded)
        if ("admin@lanchonete.com".equals(emailOuNome) && "admin123".equals(senha)) {
            Log.d(TAG, "Tentativa de login como Admin bem-sucedida (hardcoded)");
            saveUserSession(-1, "admin", "Administrador"); // Admin pode não ter um ID de cliente do banco
            navigateToRoleSpecificActivity("admin");
            return;
        }

        // Login Cozinha (hardcoded)
        if ("cozinha@lanchonete.com".equals(emailOuNome) && "cozinha123".equals(senha)) {
            Log.d(TAG, "Tentativa de login como Cozinha bem-sucedida (hardcoded)");
            saveUserSession(-2, "cozinha", "Cozinha"); // Cozinha pode não ter um ID de cliente do banco
            navigateToRoleSpecificActivity("cozinha");
            return;
        }

        // Login Cliente (via banco de dados)
        executorService.execute(() -> {
            // Tenta buscar por e-mail primeiro. Se o campo não for um e-mail,
            Cliente cliente = clienteDao.buscarPorEmail(emailOuNome);

            runOnUiThread(() -> {
                binding.progressBarLogin.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                binding.btnIrParaCadastro.setEnabled(true);

                if (cliente != null) {
                    // Verifica a senha usando BCrypt
                    if (BCrypt.checkpw(senha, cliente.getSenha())) {
                        Log.d(TAG, "Login do cliente bem-sucedido para: " + emailOuNome);
                        saveUserSession(cliente.getId(), "cliente", cliente.getNome());
                        navigateToRoleSpecificActivity("cliente");
                    } else {
                        Log.w(TAG, "Falha no login do cliente: Senha incorreta para " + emailOuNome);
                        Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Falha no login do cliente: Usuário não encontrado - " + emailOuNome);
                    Toast.makeText(this, getString(R.string.cliente_nao_encontrado), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void saveUserSession(int userId, String role, String nome) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_NOME, nome); // Salvar o nome do usuário
        editor.apply();
        Log.d(TAG, "Sessão salva: UserID=" + userId + ", Role=" + role + ", Nome=" + nome);
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
                intent = new Intent(LoginActivity.this, MainClienteActivity.class);
                break;
            default:
                // Caso inesperado, talvez voltar para o login ou mostrar erro
                Log.e(TAG, "Role desconhecido ao tentar navegar: " + role);
                Toast.makeText(this, "Erro de sessão, papel de usuário desconhecido.", Toast.LENGTH_LONG).show();
                return; // Não navega
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa a pilha de activities
        startActivity(intent);
        finish(); // Finaliza a LoginActivity para que o usuário não possa voltar para ela pressionando "back"
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
