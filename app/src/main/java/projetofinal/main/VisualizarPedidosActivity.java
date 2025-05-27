package projetofinal.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityVisualizarPedidosBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.adapters.PedidoAdapterCliente; // Usaremos este para ambos por enquanto, pode ser especializado
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class VisualizarPedidosActivity extends AppCompatActivity {

    private ActivityVisualizarPedidosBinding binding;
    private PedidoDao pedidoDao;
    private PedidoAdapterCliente pedidoAdapter;
    private List<Pedido> listaDePedidos;
    private ExecutorService executorService;
    private String userRole;
    private int clienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarVisualizarPedidos;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pedidoDao = new PedidoDao(this);
        executorService = Executors.newSingleThreadExecutor();
        listaDePedidos = new ArrayList<>();

        setupRecyclerView();

        userRole = getIntent().getStringExtra("USER_ROLE");
        clienteId = getIntent().getIntExtra("CLIENTE_ID", -1);

        if ("admin".equals(userRole)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.visualizar_todos_pedidos_title));
            }
            carregarTodosPedidos();
        } else if ("cliente".equals(userRole)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.visualizar_pedidos_title)); // "Meus Pedidos"
            }
            if (clienteId != -1) {
                carregarPedidosDoCliente(clienteId);
            } else {
                Toast.makeText(this, "ID do cliente não fornecido.", Toast.LENGTH_LONG).show();
                binding.progressBarPedidos.setVisibility(View.GONE);
                binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
            }
        } else {
            // Se o papel não for reconhecido, ou se for cliente mas sem ID
            SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
            clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
            userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");

            if ("cliente".equals(userRole) && clienteId != -1){
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R.string.visualizar_pedidos_title));
                }
                carregarPedidosDoCliente(clienteId);
            } else {
                Toast.makeText(this, "Não foi possível determinar o tipo de usuário ou ID.", Toast.LENGTH_LONG).show();
                binding.progressBarPedidos.setVisibility(View.GONE);
                binding.textViewNenhumPedido.setText("Erro ao carregar pedidos.");
                binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupRecyclerView() {
        binding.recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        pedidoAdapter = new PedidoAdapterCliente(this, listaDePedidos);
        binding.recyclerViewPedidos.setAdapter(pedidoAdapter);
    }

    private void carregarTodosPedidos() {
        binding.progressBarPedidos.setVisibility(View.VISIBLE);
        binding.textViewNenhumPedido.setVisibility(View.GONE);
        binding.recyclerViewPedidos.setVisibility(View.GONE);

        executorService.execute(() -> {
            listaDePedidos = pedidoDao.listarTodos();
            runOnUiThread(() -> {
                binding.progressBarPedidos.setVisibility(View.GONE);
                if (listaDePedidos != null && !listaDePedidos.isEmpty()) {
                    pedidoAdapter.atualizarPedidos(listaDePedidos);
                    binding.recyclerViewPedidos.setVisibility(View.VISIBLE);
                } else {
                    binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void carregarPedidosDoCliente(int idCliente) {
        binding.progressBarPedidos.setVisibility(View.VISIBLE);
        binding.textViewNenhumPedido.setVisibility(View.GONE);
        binding.recyclerViewPedidos.setVisibility(View.GONE);

        executorService.execute(() -> {
            listaDePedidos = pedidoDao.buscarPedidosPorClienteId(idCliente);
            runOnUiThread(() -> {
                binding.progressBarPedidos.setVisibility(View.GONE);
                if (listaDePedidos != null && !listaDePedidos.isEmpty()) {
                    pedidoAdapter.atualizarPedidos(listaDePedidos);
                    binding.recyclerViewPedidos.setVisibility(View.VISIBLE);
                } else {
                    binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
