package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.databinding.ActivityMainClienteBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class MainClienteActivity extends AppCompatActivity {

    private ActivityMainClienteBinding binding; // Variável para o binding
    private ClienteDao clienteDao;
    private ExecutorService executorService;
    private Cliente clienteLogado; // Para armazenar os dados do cliente logado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflar o layout usando ViewBinding
        binding = ActivityMainClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar a Toolbar
        Toolbar toolbar = binding.toolbarMainCliente; // Usando o ID do binding
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Área do Cliente");
            // Não precisa de botão de voltar aqui, a menos que seja uma sub-tela
        }

        clienteDao = new ClienteDao(this);
        executorService = Executors.newSingleThreadExecutor();

        carregarDadosClienteLogado();

        // Configurar listeners para os botões usando os IDs do binding
        binding.btnVisualizarProdutosCliente.setOnClickListener(v -> {
            Intent intent = new Intent(MainClienteActivity.this, VisualizarProdutoActivity.class);
            // Passar o ID do cliente se a VisualizarProdutoActivity precisar para o carrinho
            if (clienteLogado != null) {
                intent.putExtra("CLIENTE_ID", clienteLogado.getId());
            }
            startActivity(intent);
        });

        binding.btnVisualizarMeusPedidos.setOnClickListener(v -> {
            if (clienteLogado != null) {
                Intent intent = new Intent(MainClienteActivity.this, VisualizarPedidosActivity.class);
                intent.putExtra("CLIENTE_ID", clienteLogado.getId()); // Passando o id do cliente logado
                intent.putExtra("USER_ROLE", "cliente");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Não foi possível identificar o cliente.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLogoutCliente.setOnClickListener(v -> {
            logoutCliente();
        });
    }

    private void carregarDadosClienteLogado() {
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        String userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");

        if (clienteId != -1 && "cliente".equals(userRole)) {
            // Buscar os dados completos do cliente no banco em uma thread separada
            executorService.execute(() -> {
                clienteLogado = clienteDao.buscarPorId(clienteId);
                runOnUiThread(() -> {
                    if (clienteLogado != null) {
                        binding.textViewBemVindoCliente.setText("Bem-vindo(a), " + clienteLogado.getNome() + "!");
                    } else {
                        // Cliente não encontrado no banco, mesmo com ID salvo. Pode ter sido excluído.
                        Toast.makeText(MainClienteActivity.this, "Erro ao carregar dados do cliente.", Toast.LENGTH_SHORT).show();
                        logoutCliente(); // Força logout se dados não puderem ser carregados
                    }
                });
            });
        } else {
            // Se não houver ID de cliente ou o papel não for cliente, redireciona para login
            Toast.makeText(this, "Sessão inválida. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
            logoutCliente();
        }
    }

    private void logoutCliente() {
        // Limpar SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LoginActivity.KEY_USER_ID);
        editor.remove(LoginActivity.KEY_USER_ROLE);
        editor.apply();

        // Redirecionar para LoginActivity
        Intent intent = new Intent(MainClienteActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
