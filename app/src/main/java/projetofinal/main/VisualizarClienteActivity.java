package projetofinal.main;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;
import projetofinal.adapters.ClienteAdapter;
import com.example.projetofinal.databinding.ActivityVisualizarClienteBinding;

import java.util.List;

public class VisualizarClienteActivity extends AppCompatActivity {
    private ClienteDao clienteDao;
    private ClienteAdapter clienteAdapter;
    private ActivityVisualizarClienteBinding binding;
    private EditText editTextBuscarClienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);
        editTextBuscarClienteId = binding.editTextBuscarClienteId;

        binding.recyclerViewClientes.setLayoutManager(new LinearLayoutManager(this));

        binding.buttonBuscarPorId.setOnClickListener(v -> buscarClientePorId());

        carregarClientes();
    }

    private void carregarClientes() {
        new Thread(() -> {
            List<Cliente> clientes = clienteDao.getAllClientes();
            runOnUiThread(() -> {
                if (clientes != null && !clientes.isEmpty()) {
                    clienteAdapter = new ClienteAdapter(this, clientes);
                    binding.recyclerViewClientes.setAdapter(clienteAdapter);
                } else {
                    Toast.makeText(VisualizarClienteActivity.this, "Nenhum cliente cadastrado!", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void buscarClientePorId() {
        String idStr = editTextBuscarClienteId.getText().toString().trim();

        if (idStr.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um ID de cliente.", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = Integer.parseInt(idStr);

        new Thread(() -> {
            Cliente cliente = clienteDao.buscarPorId(id);
            runOnUiThread(() -> {
                if (cliente != null) {
                    binding.textViewNome.setText("Nome: " + cliente.getNome());
                    binding.textViewContato.setText("Contato: " + cliente.getContato());
                    binding.textViewEmail.setText("Email: " + cliente.getEmail());
                    binding.textViewSenha.setText("Senha: " + cliente.getSenha());

                    binding.textViewSenha.setVisibility(View.GONE); // Oculta o campo de senha
                } else {
                    Toast.makeText(VisualizarClienteActivity.this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
