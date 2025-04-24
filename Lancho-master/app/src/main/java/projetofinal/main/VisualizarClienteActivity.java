package projetofinal.main;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;
import projetofinal.adapters.ClienteAdapter;
import com.example.projetofinal.R;

import java.util.List;

public class VisualizarClienteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ClienteDao clienteDao;
    private ClienteAdapter clienteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_cliente);

        // Inicializa a RecyclerView e o Adapter
        recyclerView = findViewById(R.id.recycler_view_clientes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // LinearLayoutManager para exibição vertical

        clienteDao = new ClienteDao(this);

        // Carregar os clientes no RecyclerView
        carregarClientes();
    }

    // Metodo para carregar os clientes no RecyclerView
    private void carregarClientes() {
        // Executa uma tarefa para acessar o banco de dados e obter a lista de clientes
        new Thread(() -> {
            List<Cliente> clientes = clienteDao.getAllClientes(); // Busca todos os clientes do banco
            runOnUiThread(() -> {
                if (clientes != null && !clientes.isEmpty()) {
                    // Inicializa o Adapter e configura a RecyclerView
                    clienteAdapter = new ClienteAdapter(this, clientes);
                    recyclerView.setAdapter(clienteAdapter);
                } else {
                    // Caso não haja clientes, exibe uma mensagem
                    Toast.makeText(VisualizarClienteActivity.this, "Nenhum cliente cadastrado!", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
