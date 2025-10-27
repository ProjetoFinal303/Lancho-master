package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat; // <<<--- IMPORTAÇÃO ADICIONADA AQUI
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
    private static final String TAG = "MainClienteActivity"; // Tag for logging

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
        // *** Chamada para carregar dados do cliente ***
        carregarDadosClienteLogado();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Destaque");
            // *** Define o ícone padrão IMEDIATAMENTE ***
            toolbar.setNavigationIcon(R.drawable.ic_person);
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
                return false; // Não seleciona o item do carrinho na barra
            }
            return false;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss(); // Use commitAllowingStateLoss if needed for flexibility
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_cliente_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // android.R.id.home corresponde ao ícone de navegação (seja a foto ou ic_person)
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

        // *** Define o ícone padrão ANTES da chamada assíncrona ***
        runOnUiThread(() -> {
            if (binding != null && binding.toolbarMainCliente != null) {
                binding.toolbarMainCliente.setNavigationIcon(R.drawable.ic_person);
                Log.d(TAG, "Ícone padrão ic_person definido inicialmente.");
            }
        });


        if (clienteId == -1) {
            Log.e(TAG, "ID do cliente inválido (-1). Fazendo logout.");
            logoutCliente(); // Se o ID for inválido, desloga
            return;
        }

        Log.d(TAG, "Buscando dados para o cliente ID: " + clienteId);
        clienteDao.buscarPorId(clienteId, cliente -> {
            // *** Só tenta carregar a foto SE tiver uma URL válida ***
            if (cliente != null && cliente.getAvatarUrl() != null && !cliente.getAvatarUrl().isEmpty()) {
                Log.d(TAG, "Cliente encontrado. Tentando carregar URL: " + cliente.getAvatarUrl());
                // Chama o método para carregar a foto na thread principal
                runOnUiThread(() -> setProfileIcon(cliente.getAvatarUrl()));
            } else {
                // Se não tem URL ou cliente é nulo, o ic_person (já definido) permanece. Log apenas confirma.
                String reason = (cliente == null) ? "Cliente nulo" : "URL da foto nula ou vazia";
                Log.d(TAG, reason + ". Ícone padrão ic_person mantido.");
                // Garantir que ic_person esteja setado caso algo tenha mudado (redundante, mas seguro)
                runOnUiThread(() -> {
                    if (binding != null && binding.toolbarMainCliente != null) {
                        binding.toolbarMainCliente.setNavigationIcon(R.drawable.ic_person);
                    }
                });
            }
        }, error -> {
            // Em caso de erro na busca, o ic_person (já definido) também permanece.
            Log.e(TAG, "Erro ao buscar cliente: ", error);
            // Garantir que ic_person esteja setado (redundante, mas seguro)
            runOnUiThread(() -> {
                if (binding != null && binding.toolbarMainCliente != null) {
                    binding.toolbarMainCliente.setNavigationIcon(R.drawable.ic_person);
                }
            });
        });
    }

    private void setProfileIcon(String url) {
        // Verifica se binding e toolbar ainda são válidos antes de usar
        if (binding == null || binding.toolbarMainCliente == null) {
            Log.w(TAG, "setProfileIcon chamado, mas binding ou toolbar é nulo.");
            return;
        }
        Toolbar toolbar = binding.toolbarMainCliente;
        int iconSize = getResources().getDimensionPixelSize(R.dimen.toolbar_profile_icon_size);

        Log.d(TAG, "Glide iniciando carregamento para URL: " + url);
        Glide.with(this)
                .load(url)
                .circleCrop()
                .override(iconSize, iconSize)
                // Define o ic_person como placeholder E como imagem de erro
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // Verifica novamente se a toolbar existe antes de setar o ícone
                        if (binding != null && binding.toolbarMainCliente != null) {
                            Log.d(TAG, "Glide onResourceReady - Definindo imagem carregada.");
                            toolbar.setNavigationIcon(resource);
                        } else {
                            Log.w(TAG, "Glide onResourceReady, mas toolbar se tornou nula.");
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Se o carregamento for cancelado ou falhar, garante o ic_person
                        if (binding != null && binding.toolbarMainCliente != null) {
                            Log.d(TAG, "Glide onLoadCleared - Definindo ic_person.");
                            toolbar.setNavigationIcon(R.drawable.ic_person);
                        } else {
                            Log.w(TAG, "Glide onLoadCleared, mas toolbar se tornou nula.");
                        }
                    }
                    // Adicionando tratamento de erro explícito do Glide
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        if (binding != null && binding.toolbarMainCliente != null) {
                            Log.e(TAG, "Glide onLoadFailed - Definindo ic_person (ou drawable de erro).");
                            // Usa o drawable de erro passado pelo Glide, que configuramos para ser ic_person
                            // AQUI ESTÁ A CORREÇÃO PRINCIPAL: Adicionada a importação de ContextCompat
                            toolbar.setNavigationIcon(errorDrawable != null ? errorDrawable : ContextCompat.getDrawable(MainClienteActivity.this, R.drawable.ic_person));
                        } else {
                            Log.w(TAG, "Glide onLoadFailed, mas toolbar se tornou nula.");
                        }
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

    // Método para atualizar o ícone do carrinho (se existir no menu)
    public void atualizarBotaoCarrinho() {
        invalidateOptionsMenu(); // Força a recriação do menu de opções
    }

    // Limpa o binding para evitar memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}