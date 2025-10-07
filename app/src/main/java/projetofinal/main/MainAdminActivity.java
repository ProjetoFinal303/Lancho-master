package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
            getSupportActionBar().setTitle(R.string.admin_panel_title);
        }

        setupListeners();
    }

    private void setupListeners() {
        binding.cardSincronizarProdutos.setOnClickListener(v -> {
            Toast.makeText(this, "Iniciando sincronização com o Stripe...", Toast.LENGTH_LONG).show();
            // TODO: Implementar a chamada para a Edge Function 'sync-stripe-products'
        });

        binding.cardGerenciarProdutos.setOnClickListener(v -> {
            startActivity(new Intent(MainAdminActivity.this, VisualizarProdutoActivity.class));
        });

        // O CARD DE ESTOQUE FOI REMOVIDO DO LAYOUT E SEU LISTENER FOI REMOVIDO DAQUI

        binding.cardVisualizarPedidos.setOnClickListener(v -> {
            Intent intent = new Intent(MainAdminActivity.this, VisualizarPedidosActivity.class);
            intent.putExtra("USER_ROLE", "admin");
            startActivity(intent);
        });

        binding.cardGerenciarClientes.setOnClickListener(v -> {
            startActivity(new Intent(MainAdminActivity.this, VisualizarClienteActivity.class));
        });

        binding.cardLogoutAdmin.setOnClickListener(v -> {
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