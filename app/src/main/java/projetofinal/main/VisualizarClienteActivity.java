package projetofinal.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.databinding.ActivityVisualizarClienteBinding;
import java.util.ArrayList;
import java.util.List;
import projetofinal.adapters.ClienteAdapter;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class VisualizarClienteActivity extends AppCompatActivity {
    private ClienteDao clienteDao;
    private ClienteAdapter clienteAdapter;
    private ActivityVisualizarClienteBinding binding;
    private List<Cliente> todosClientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        binding.recyclerViewClientes.setLayoutManager(new LinearLayoutManager(this));
        clienteAdapter = new ClienteAdapter(this, new ArrayList<>(), cliente -> {
            // Lógica de clique no item, se houver
        });
        binding.recyclerViewClientes.setAdapter(clienteAdapter);
    }

    private void carregarTodosClientes() {
        setLoading(true);
        clienteDao.getAllClientes(
                clientes -> runOnUiThread(() -> {
                    setLoading(false);
                    if (clientes != null && !clientes.isEmpty()) {
                        todosClientes.clear();
                        todosClientes.addAll(clientes);
                        clienteAdapter.updateClientList(todosClientes);
                        binding.recyclerViewClientes.setVisibility(View.VISIBLE);
                        binding.textViewNenhumCliente.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewClientes.setVisibility(View.GONE);
                        binding.textViewNenhumCliente.setVisibility(View.VISIBLE);
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro ao carregar clientes.", Toast.LENGTH_SHORT).show();
                    binding.textViewNenhumCliente.setVisibility(View.VISIBLE);
                })
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTodosClientes();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarVisualizar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}