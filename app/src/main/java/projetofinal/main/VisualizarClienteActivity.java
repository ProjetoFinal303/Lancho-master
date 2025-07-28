package projetofinal.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.databinding.ActivityVisualizarClienteBinding;
import java.util.ArrayList;
import java.util.List;
import projetofinal.adapters.ClienteAdapter;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class VisualizarClienteActivity extends BaseActivity {
    private ClienteDao clienteDao;
    private ClienteAdapter clienteAdapter;
    private ActivityVisualizarClienteBinding binding;
    private List<Cliente> todosClientes = new ArrayList<>();
    private static final String TAG = "VisualizarCliente";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);
        setupRecyclerView();

        // Removemos a lógica de busca por ID, pois a lista é mais prática
        binding.tilBuscarClienteId.setVisibility(View.GONE);
        binding.buttonBuscarPorId.setVisibility(View.GONE);
        binding.buttonLimparBusca.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        binding.recyclerViewClientes.setLayoutManager(new LinearLayoutManager(this));

        clienteAdapter = new ClienteAdapter(todosClientes, new ClienteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Cliente cliente) {
                // Aqui você pode adicionar uma ação ao clicar no card, como editar
                Toast.makeText(VisualizarClienteActivity.this, "Cliente selecionado: " + cliente.getNome(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Cliente cliente) {
                // Pede confirmação antes de excluir
                mostrarDialogoConfirmacao(cliente);
            }
        });

        binding.recyclerViewClientes.setAdapter(clienteAdapter);
    }

    private void mostrarDialogoConfirmacao(Cliente cliente) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o cliente " + cliente.getNome() + "?")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> excluirCliente(cliente))
                .setNegativeButton("Não", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void excluirCliente(Cliente cliente) {
        setLoading(true);
        clienteDao.excluir(cliente.getId(),
                response -> runOnUiThread(() -> {
                    Toast.makeText(this, "Cliente excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    carregarTodosClientes(); // Recarrega a lista para remover o cliente
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao excluir: ", error);
                    Toast.makeText(this, "Falha ao excluir cliente.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void carregarTodosClientes() {
        setLoading(true);
        clienteDao.getAllClientes(
                clientes -> runOnUiThread(() -> {
                    setLoading(false);
                    if (clientes != null && !clientes.isEmpty()) {
                        todosClientes = clientes;
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