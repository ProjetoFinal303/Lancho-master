package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View; // <<<--- CORREÇÃO AQUI
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityLoginBinding;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private ClienteDao clienteDao;

    public static final String PREFS_NAME = "UserSessionPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_ROLE = "userRole";
    public static final String KEY_USER_NOME = "userNome";

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

        binding.btnLogin.setOnClickListener(v -> attemptManualLogin());
        binding.btnIrParaCadastro.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, CadastrarClienteActivity.class)));

        setupThemeButton();
        setupLanguageButton();
    }

    private void setupThemeButton() {
        binding.btnThemeToggle.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int newNightMode = (currentNightMode == Configuration.UI_MODE_NIGHT_YES) ?
                    AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

            SharedPreferences prefs = getSharedPreferences(SettingsActivity.THEME_PREFS, MODE_PRIVATE);
            prefs.edit().putInt(SettingsActivity.KEY_THEME_MODE, newNightMode).apply();
            AppCompatDelegate.setDefaultNightMode(newNightMode);
        });
        updateThemeToggleButton();
    }

    private void setupLanguageButton() {
        binding.btnLanguage.setOnClickListener(v -> {
            final String[] languages = {
                    getString(R.string.portuguese),
                    getString(R.string.english),
                    getString(R.string.spanish)
            };
            final String[] langCodes = {"pt", "en", "es"};

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.language_label))
                    .setItems(languages, (dialog, which) -> {
                        LocaleHelper.setLocale(this, langCodes[which]);
                        recreate();
                    })
                    .show();
        });
    }

    private void updateThemeToggleButton() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            binding.btnThemeToggle.setImageResource(R.drawable.ic_sun);
        } else {
            binding.btnThemeToggle.setImageResource(R.drawable.ic_moon);
        }
    }

    private void attemptManualLogin() {
        String email = binding.edtEmail.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, R.string.login_fields_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if ("cozinha@lancho.com".equalsIgnoreCase(email) && "13032025info".equals(senha)) {
            setLoading(true);
            Toast.makeText(this, "Bem-vindo(a), Cozinha!", Toast.LENGTH_SHORT).show();
            saveUserSession(-1, "cozinha", "Cozinha");
            navigateToRoleSpecificActivity("cozinha");
            return;
        }

        setLoading(true);

        clienteDao.buscarPorEmail(email,
                cliente -> runOnUiThread(() -> {
                    setLoading(false);
                    if (cliente != null && cliente.getSenha().equals(senha)) {
                        if ("admin".equalsIgnoreCase(cliente.getRole())) {
                            saveUserSession(cliente.getId(), "admin", cliente.getNome());
                            navigateToRoleSpecificActivity("admin");
                        } else {
                            saveUserSession(cliente.getId(), "cliente", cliente.getNome());
                            navigateToRoleSpecificActivity("cliente");
                        }
                    } else {
                        Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro de conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnIrParaCadastro.setEnabled(!isLoading);
        binding.tilEmail.setEnabled(!isLoading);
        binding.tilSenha.setEnabled(!isLoading);
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

