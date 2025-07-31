package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityMainClienteBinding;
import projetofinal.dao.ClienteDao;

public class MainClienteActivity extends BaseActivity {

    private ActivityMainClienteBinding binding;
    private ClienteDao clienteDao;

    // Objeto público para que o fragmento possa acessá-lo
    public static class ProdutoDestaque {
        public final String nome;
        public final String descricao;
        public final int drawableId;

        ProdutoDestaque(String nome, String descricao, int drawableId) {
            this.nome = nome;
            this.descricao = descricao;
            this.drawableId = drawableId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarMainCliente;
        setSupportActionBar(toolbar);

        clienteDao = new ClienteDao(this);
        carregarDadosClienteLogado();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Destaque");
        }

        if (savedInstanceState == null) {
            replaceFragment(new DestaqueFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_destaque);
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_destaque) {
                replaceFragment(new DestaqueFragment());
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Destaque");
                return true;
            } else if (itemId == R.id.nav_cardapio) {
                replaceFragment(new CardapioFragment());
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Cardápio");
                return true;
            } else if (itemId == R.id.nav_pedidos) {
                replaceFragment(new PedidosFragment());
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Meus Pedidos");
                return true;
            } else if (itemId == R.id.nav_carrinho) {
                startActivity(new Intent(this, CadastrarPedidoActivity.class));
                return false;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_cliente_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

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

    // Método público para o DestaqueFragment chamar
    public ProdutoDestaque getProdutoDestaqueDoDia() {
        // A sua lógica de destaque continua aqui
        return new ProdutoDestaque("Lanchô X-Burger", "Nosso clássico X-Burger com pão fofinho e molho especial.", R.drawable.lanchoxburger);
    }

    private void carregarDadosClienteLogado() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        if (clienteId == -1) {
            logoutCliente();
            return;
        }

        clienteDao.buscarPorId(clienteId, cliente -> {
            if (cliente != null && cliente.getAvatarUrl() != null && !cliente.getAvatarUrl().isEmpty()) {
                runOnUiThread(() -> setProfileIcon(cliente.getAvatarUrl()));
            } else {
                runOnUiThread(() -> {
                    if (getSupportActionBar() != null) {
                        binding.toolbarMainCliente.setNavigationIcon(R.drawable.ic_person);
                    }
                });
            }
        }, error -> runOnUiThread(() -> {
            if (getSupportActionBar() != null) {
                binding.toolbarMainCliente.setNavigationIcon(R.drawable.ic_person);
            }
        }));
    }

    private void setProfileIcon(String url) {
        Toolbar toolbar = binding.toolbarMainCliente;
        int iconSize = getResources().getDimensionPixelSize(R.dimen.toolbar_profile_icon_size);

        Glide.with(this)
                .load(url)
                .circleCrop()
                .override(iconSize, iconSize)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        toolbar.setNavigationIcon(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        toolbar.setNavigationIcon(R.drawable.ic_person);
                    }
                });
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