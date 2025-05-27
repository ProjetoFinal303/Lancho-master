package projetofinal.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityVisualizarClienteBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.adapters.ClienteAdapter;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class VisualizarClienteActivity extends AppCompatActivity {
    private ClienteDao clienteDao;
    private ClienteAdapter clienteAdapter;
    private ActivityVisualizarClienteBinding binding;
    private ExecutorService executorService;
    private List<Cliente> todosClientes = new ArrayList<>();
    private String userRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarVisualizarClientes; // Certifique-se que este ID existe no XML
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.visualizar_clientes_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        clienteDao = new ClienteDao(this);
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");

        setupRecyclerView();

        binding.buttonBuscarPorId.setOnClickListener(v -> buscarClientePorId());
        binding.buttonLimparBusca.setOnClickListener(v -> {
            binding.editTextBuscarClienteId.setText("");
            binding.layoutDadosClienteEspecifico.setVisibility(View.GONE);
            if (clienteAdapter != null) {
                clienteAdapter.updateClientList(new ArrayList<>(todosClientes)); // Mostra todos novamente
            }
            binding.recyclerViewClientes.setVisibility(View.VISIBLE);
            binding.textViewNenhumCliente.setVisibility(todosClientes.isEmpty() ? View.VISIBLE : View.GONE);
        });
        // carregarTodosClientes() é chamado no onResume
    }

    private void setupRecyclerView() {
        binding.recyclerViewClientes.setLayoutManager(new LinearLayoutManager(this));
        clienteAdapter = new ClienteAdapter(this, new ArrayList<>(), cliente -> {
            if ("admin".equals(userRole)) {
                mostrarOpcoesAdmin(cliente);
            } else {
                // Para cliente normal, clicar no item pode não fazer nada ou mostrar mais detalhes
                Toast.makeText(this, "Cliente: " + cliente.getNome(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerViewClientes.setAdapter(clienteAdapter);
    }

    private void mostrarOpcoesAdmin(Cliente cliente) {
        CharSequence[] opcoes = {"Atualizar Cliente", "Excluir Cliente"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opções para: " + cliente.getNome());
        builder.setItems(opcoes, (dialog, which) -> {
            if (which == 0) { // Atualizar Cliente
                Intent intent = new Intent(VisualizarClienteActivity.this, AtualizarClienteActivity.class);
                intent.putExtra("CLIENTE_ID", cliente.getId());
                startActivity(intent);
            } else if (which == 1) { // Excluir Cliente
                // Navega para ExcluirClienteActivity passando o ID
                // ou implementa a lógica de exclusão diretamente aqui com confirmação
                new AlertDialog.Builder(VisualizarClienteActivity.this)
                        .setTitle(getString(R.string.confirmar_exclusao_title))
                        .setMessage(getString(R.string.confirmar_exclusao_cliente_message, cliente.getNome(), cliente.getId()))
                        .setPositiveButton(getString(R.string.sim), (d, w) -> excluirClienteSelecionado(cliente))
                        .setNegativeButton(getString(R.string.nao), null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        builder.show();
    }

    private void excluirClienteSelecionado(Cliente cliente) {
        binding.progressBarVisualizar.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            // Lembre-se que excluir um cliente pode afetar pedidos (ON DELETE CASCADE no DB)
            // e outras entidades relacionadas.
            int linhasAfetadas = clienteDao.excluir(cliente.getId());
            runOnUiThread(() -> {
                binding.progressBarVisualizar.setVisibility(View.GONE);
                if (linhasAfetadas > 0) {
                    Toast.makeText(VisualizarClienteActivity.this, getString(R.string.cliente_excluido_sucesso), Toast.LENGTH_SHORT).show();
                    carregarTodosClientes(); // Recarrega a lista
                } else {
                    Toast.makeText(VisualizarClienteActivity.this, getString(R.string.falha_excluir_cliente), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    private void carregarTodosClientes() {
        binding.progressBarVisualizar.setVisibility(View.VISIBLE);
        binding.textViewNenhumCliente.setVisibility(View.GONE);
        binding.recyclerViewClientes.setVisibility(View.GONE);
        binding.layoutDadosClienteEspecifico.setVisibility(View.GONE);


        executorService.execute(() -> {
            // Armazena a lista carregada na variável de instância
            List<Cliente> clientesCarregados = clienteDao.getAllClientes();
            // Cria uma cópia para a UI thread
            final List<Cliente> finalClientesCarregados = new ArrayList<>(clientesCarregados);

            runOnUiThread(() -> {
                todosClientes.clear();
                todosClientes.addAll(finalClientesCarregados);

                binding.progressBarVisualizar.setVisibility(View.GONE);
                if (!todosClientes.isEmpty()) {
                    clienteAdapter.updateClientList(new ArrayList<>(todosClientes)); // Passa cópia para o adapter
                    binding.recyclerViewClientes.setVisibility(View.VISIBLE);
                    binding.textViewNenhumCliente.setVisibility(View.GONE);
                } else {
                    binding.recyclerViewClientes.setVisibility(View.GONE);
                    binding.textViewNenhumCliente.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void buscarClientePorId() {
        String idStr = binding.editTextBuscarClienteId.getText().toString().trim();

        if (TextUtils.isEmpty(idStr)) {
            Toast.makeText(this, getString(R.string.informe_id_generico, "cliente"), Toast.LENGTH_SHORT).show();
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.id_invalido), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarVisualizar.setVisibility(View.VISIBLE);
        binding.layoutDadosClienteEspecifico.setVisibility(View.GONE);
        binding.recyclerViewClientes.setVisibility(View.GONE);
        binding.textViewNenhumCliente.setVisibility(View.GONE);


        executorService.execute(() -> {
            Cliente cliente = clienteDao.buscarPorId(id);
            runOnUiThread(() -> {
                binding.progressBarVisualizar.setVisibility(View.GONE);
                if (cliente != null) {
                    binding.textViewNomeClienteEspecifico.setText(getString(R.string.dados_cliente_nome, cliente.getNome()));
                    binding.textViewEmailClienteEspecifico.setText(getString(R.string.dados_cliente_email, cliente.getEmail()));
                    binding.textViewContatoClienteEspecifico.setText(getString(R.string.dados_cliente_contato, cliente.getContato()));
                    binding.layoutDadosClienteEspecifico.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(VisualizarClienteActivity.this, getString(R.string.cliente_nao_encontrado_id, id), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTodosClientes(); // Sempre recarrega a lista de clientes ao voltar para esta tela
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