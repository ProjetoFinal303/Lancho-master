package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityMainAdminBinding;

public class MainAdminActivity extends AppCompatActivity {

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

        binding.btnGerenciarProdutos.setOnClickListener(v -> {
            // Navegar para uma activity que lista produtos e permite Add/Edit/Delete
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
            startActivity(new Intent(MainAdminActivity.this, VisualizarClienteActivity.class));
        });

        // Listener para o novo botão
        binding.btnExcluirClienteAdmin.setOnClickListener(v -> {
            startActivity(new Intent(MainAdminActivity.this, ExcluirClienteActivity.class));
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
