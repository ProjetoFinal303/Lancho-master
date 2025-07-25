package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityMainClienteBinding;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class MainClienteActivity extends AppCompatActivity {

    private ActivityMainClienteBinding binding;
    private ClienteDao clienteDao;
    private Cliente clienteLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);
        carregarDadosClienteLogado();

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

    private void carregarDadosClienteLogado() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (clienteId == -1) { logoutCliente(); return; }

        clienteDao.buscarPorId(clienteId,
                cliente -> runOnUiThread(() -> {
                    if (cliente != null) {
                        clienteLogado = cliente;
                        binding.textViewBemVindoCliente.setText("Bem-vindo(a), " + cliente.getNome() + "!");
                    } else {
                        Toast.makeText(this, "Erro ao carregar dados do cliente.", Toast.LENGTH_SHORT).show();
                        logoutCliente();
                    }
                }),
                error -> runOnUiThread(() -> {
                    Toast.makeText(this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
                    logoutCliente();
                })
        );
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