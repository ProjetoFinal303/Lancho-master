package projetofinal.main;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.R;

import java.util.List;

import projetofinal.adapters.PedidoAdapter;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class VisualizarPedidosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPedidos;
    private PedidoAdapter pedidoAdapter;
    private PedidoDao pedidoDao;
    private int clienteId; // para buscar apenas os pedidos do cliente logado

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_pedidos);

        recyclerViewPedidos = findViewById(R.id.recyclerViewPedidos);
        recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(this));

        pedidoDao = new PedidoDao(this); // Passando o contexto para o PedidoDao


        // Recebe o ID do cliente logado
        clienteId = getIntent().getIntExtra("cliente_id", -1);

        if (clienteId != -1) {
            carregarPedidosDoCliente(clienteId);
        } else {
            Toast.makeText(this, "Erro ao carregar pedidos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarPedidosDoCliente(int clienteId) {
        // Agora, você pode usar o PedidoDao para buscar os pedidos do cliente
        List<Pedido> pedidos = pedidoDao.buscarPedidosPorClienteId(clienteId); // Buscar pedidos específicos por cliente
        if (pedidos != null && !pedidos.isEmpty()) {
            pedidoAdapter = new PedidoAdapter(pedidos);
            recyclerViewPedidos.setAdapter(pedidoAdapter);
        } else {
            Toast.makeText(this, "Nenhum pedido encontrado.", Toast.LENGTH_SHORT).show();
        }
    }
}
