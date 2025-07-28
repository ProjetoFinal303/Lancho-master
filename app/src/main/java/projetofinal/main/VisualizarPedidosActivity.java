package projetofinal.main;

import android.os.Bundle;
import android.util.Log;
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
import projetofinal.adapters.PedidoAdapterCliente;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class VisualizarPedidosActivity extends BaseActivity {

    private ActivityVisualizarPedidosBinding binding;
    private PedidoDao pedidoDao;
    private PedidoAdapterCliente pedidoAdapter;
    private List<Pedido> listaDePedidos = new ArrayList<>();
    private static final String TAG = "VisualizarPedidos";
    private boolean isAdmin = false;

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

        String userRole = getIntent().getStringExtra("USER_ROLE");
        int clienteId = getIntent().getIntExtra("CLIENTE_ID", -1);

        if ("admin".equals(userRole)) {
            isAdmin = true;
            getSupportActionBar().setTitle(getString(R.string.visualizar_todos_pedidos_title));
            setupRecyclerView();
            carregarTodosPedidos();
        } else {
            isAdmin = false;
            getSupportActionBar().setTitle(getString(R.string.visualizar_pedidos_title));
            setupRecyclerView();
            if (clienteId != -1) {
                carregarPedidosDoCliente(clienteId);
            } else {
                Toast.makeText(this, "ID do cliente não encontrado.", Toast.LENGTH_LONG).show();
                binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupRecyclerView() {
        binding.recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));
        // Agora passamos a variável 'isAdmin' para o adaptador
        pedidoAdapter = new PedidoAdapterCliente(this, listaDePedidos, isAdmin);
        binding.recyclerViewPedidos.setAdapter(pedidoAdapter);
    }

    private void carregarTodosPedidos() {
        setLoading(true);
        pedidoDao.listarTodos(
                pedidos -> runOnUiThread(() -> {
                    setLoading(false);
                    atualizarLista(pedidos);
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao carregar todos os pedidos: ", error);
                    Toast.makeText(this, "Erro ao carregar pedidos.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void carregarPedidosDoCliente(int idCliente) {
        setLoading(true);
        pedidoDao.buscarPedidosPorClienteId(idCliente,
                pedidos -> runOnUiThread(() -> {
                    setLoading(false);
                    atualizarLista(pedidos);
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao carregar pedidos do cliente: ", error);
                    Toast.makeText(this, "Erro ao carregar seus pedidos.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void atualizarLista(List<Pedido> pedidos) {
        if (pedidos != null && !pedidos.isEmpty()) {
            listaDePedidos.clear();
            listaDePedidos.addAll(pedidos);
            // Ordena a lista para mostrar os pedidos mais recentes primeiro
            listaDePedidos.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
            pedidoAdapter.atualizarPedidos(listaDePedidos);
            binding.recyclerViewPedidos.setVisibility(View.VISIBLE);
            binding.textViewNenhumPedido.setVisibility(View.GONE);
        } else {
            binding.recyclerViewPedidos.setVisibility(View.GONE);
            binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarPedidos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}