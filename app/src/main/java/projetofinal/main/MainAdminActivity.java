package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast; // Import necessário para o Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityMainAdminBinding;

public class MainAdminActivity extends BaseActivity {

    private ActivityMainAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarMainAdmin;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Painel Administrativo");
        }

        // Listener para o novo botão de Sincronização
        binding.btnSincronizarProdutos.setOnClickListener(v -> {
            // A implementação da chamada de rede para a Edge Function precisa ser criada.
            // Por enquanto, exibimos uma mensagem ao usuário.
            Toast.makeText(this, "Iniciando sincronização com o Stripe...", Toast.LENGTH_LONG).show();

            // TODO: Implementar a chamada para a Edge Function 'sync-stripe-products'
            // usando um serviço de rede autenticado (ex: OkHttp com o token JWT do usuário).
        });

        binding.btnGerenciarProdutos.setOnClickListener(v -> {
            // A tela VisualizarProdutoActivity agora mostra os produtos do Stripe
            startActivity(new Intent(MainAdminActivity.this, VisualizarProdutoActivity.class));
        });

        binding.btnGerenciarEstoque.setOnClickListener(v -> {
            startActivity(new Intent(MainAdminActivity.this, VisualizarEstoqueActivity.class));
        });

        binding.btnVisualizarPedidos.setOnClickListener(v -> {
            Intent intent = new Intent(MainAdminActivity.this, VisualizarPedidosActivity.class);
            intent.putExtra("USER_ROLE", "admin");
            startActivity(intent);
        });

        binding.btnGerenciarClientes.setOnClickListener(v -> {
            // A tela VisualizarClienteActivity agora inclui a função de excluir
            startActivity(new Intent(MainAdminActivity.this, VisualizarClienteActivity.class));
        });

        binding.btnLogoutAdmin.setOnClickListener(v -> {
            logoutAdmin();
        });
    }

    private void logoutAdmin() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LoginActivity.KEY_USER_ID);
        editor.remove(LoginActivity.KEY_USER_ROLE);
        editor.remove(LoginActivity.KEY_USER_NOME);
        editor.apply();

        Intent intent = new Intent(MainAdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}