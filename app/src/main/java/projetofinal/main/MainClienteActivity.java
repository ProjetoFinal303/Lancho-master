package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityMainClienteBinding;
import projetofinal.models.Cliente;

public class MainClienteActivity extends BaseActivity {

    private ActivityMainClienteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ---> INÍCIO DA CORREÇÃO <---
        // Configura o ícone de navegação diretamente na Toolbar
        binding.toolbarMainCliente.setNavigationIcon(R.drawable.ic_person);
        setSupportActionBar(binding.toolbarMainCliente);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Destaque");
        }
        // ---> FIM DA CORREÇÃO <---

        carregarDadosClienteLogado();

        if (savedInstanceState == null) {
            replaceFragment(new DestaqueFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_destaque);
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_destaque) {
                replaceFragment(new DestaqueFragment());
                getSupportActionBar().setTitle("Destaque");
                return true;
            } else if (itemId == R.id.nav_cardapio) {
                replaceFragment(new CardapioFragment());
                getSupportActionBar().setTitle("Cardápio");
                return true;
            } else if (itemId == R.id.nav_pedidos) {
                replaceFragment(new PedidosFragment());
                getSupportActionBar().setTitle("Meus Pedidos");
                return true;
            } else if (itemId == R.id.nav_carrinho) {
                startActivity(new Intent(this, CadastrarPedidoActivity.class));
                return false;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_cliente_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // O ID 'android.R.id.home' é o ID padrão para o ícone de navegação (à esquerda)
        if (itemId == android.R.id.home) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void carregarDadosClienteLogado() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        if (clienteId == -1) {
            logoutCliente();
        }
    }

    public void logoutCliente() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void atualizarBotaoCarrinho() {
        invalidateOptionsMenu();
    }
}