package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityMainClienteBinding;
import java.util.ArrayList;
import java.util.List;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class MainClienteActivity extends BaseActivity {

    private ActivityMainClienteBinding binding;
    private ClienteDao clienteDao;
    private Cliente clienteLogado;

    private static class ProdutoDestaque {
        final String nome;
        final String descricao;
        final int drawableId;

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

        setSupportActionBar(binding.toolbarMainCliente);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // Título vazio
        }

        clienteDao = new ClienteDao(this);
        carregarDadosClienteLogado();
        carregarDestaqueDoDia();

        binding.btnVisualizarProdutosCliente.setOnClickListener(v -> {
            startActivity(new Intent(this, VisualizarProdutoActivity.class));
        });

        binding.btnVisualizarMeusPedidos.setOnClickListener(v -> {
            if (clienteLogado != null) {
                Intent intent = new Intent(this, VisualizarPedidosActivity.class);
                intent.putExtra("CLIENTE_ID", clienteLogado.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Aguarde, carregando dados...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLogoutCliente.setOnClickListener(v -> logoutCliente());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_cliente_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void carregarDestaqueDoDia() {
        List<ProdutoDestaque> produtos = new ArrayList<>();
        produtos.add(new ProdutoDestaque("Lanchô X-Burger", "Nosso clássico X-Burger com pão fofinho e molho especial.", R.drawable.lanchoxburger));
        produtos.add(new ProdutoDestaque("Batata Frita da Casa", "Porção generosa de batatas super crocantes, perfeitas para acompanhar.", R.drawable.batatafrita));
        produtos.add(new ProdutoDestaque("Coca-Cola 2L", "Para refrescar e acompanhar o seu lanche, uma Coca-Cola bem gelada.", R.drawable.cocacola));
        produtos.add(new ProdutoDestaque("MilkShake Especial", "Cremoso e delicioso, feito com sorvete de verdade e uma calda especial.", R.drawable.milkshake));
        produtos.add(new ProdutoDestaque("Onion Rings da Casa", "Anéis de cebola empanados e fritos na perfeição. Crocantes e ideais para compartilhar.", R.drawable.onionrings));

        long daysSinceEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        int productIndex = (int) (daysSinceEpoch % produtos.size());
        ProdutoDestaque destaque = produtos.get(productIndex);

        binding.textViewNomeDestaque.setText(destaque.nome);
        binding.textViewDescricaoDestaque.setText(destaque.descricao);
        Glide.with(this)
                .load(destaque.drawableId)
                .into(binding.imageViewDestaque);
    }

    private void carregarDadosClienteLogado() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        String nomeCliente = prefs.getString(LoginActivity.KEY_USER_NOME, "");

        if (clienteId != -1) {
            clienteLogado = new Cliente();
            clienteLogado.setId(clienteId);
            clienteLogado.setNome(nomeCliente);
        } else {
            logoutCliente();
        }
    }

    private void logoutCliente() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}