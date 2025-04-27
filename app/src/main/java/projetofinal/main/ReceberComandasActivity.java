package projetofinal.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.R;

import java.util.List;

import projetofinal.adapters.PedidoAdapter;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class ReceberComandasActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PedidoAdapter adapter;
    private PedidoDao pedidoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receber_comandas);

        recyclerView = findViewById(R.id.recyclerViewPedidos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pedidoDao = new PedidoDao(this);
        List<Pedido> pedidos = pedidoDao.listarTodos();

        adapter = new PedidoAdapter(pedidos);
        recyclerView.setAdapter(adapter);
    }
}
