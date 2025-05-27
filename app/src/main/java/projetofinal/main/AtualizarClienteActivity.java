package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityAtualizarClienteBinding;
import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class AtualizarClienteActivity extends AppCompatActivity {

    private ActivityAtualizarClienteBinding binding;
    private ClienteDao clienteDao;
    private Cliente clienteSelecionado;
    private ExecutorService executorService;
    private int clienteIdToLoad = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtualizarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);
        executorService = Executors.newSingleThreadExecutor();

        if (getIntent().hasExtra("CLIENTE_ID")) {
            clienteIdToLoad = getIntent().getIntExtra("CLIENTE_ID", -1);
            binding.edtId.setText(String.valueOf(clienteIdToLoad));
            binding.edtId.setEnabled(false); // Disable ID editing if passed via Intent
            buscarClienteParaAtualizar(clienteIdToLoad);
        }


        binding.btnBuscar.setOnClickListener(v -> {
            String idStr = binding.edtId.getText().toString().trim();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(this, "Informe o ID do cliente!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int id = Integer.parseInt(idStr);
                buscarClienteParaAtualizar(id);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "ID inválido!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnAtualizar.setOnClickListener(v -> atualizarDadosCliente());
    }

    private void buscarClienteParaAtualizar(int id) {
        if (id == -1) return;

        binding.progressBarAtualizar.setVisibility(View.VISIBLE);
        binding.fieldsGroup.setVisibility(View.GONE);

        executorService.execute(() -> {
            clienteSelecionado = clienteDao.buscarPorId(id);
            runOnUiThread(() -> {
                binding.progressBarAtualizar.setVisibility(View.GONE);
                if (clienteSelecionado == null) {
                    Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
                    binding.fieldsGroup.setVisibility(View.GONE);
                } else {
                    binding.edtNome.setText(clienteSelecionado.getNome());
                    binding.edtEmail.setText(clienteSelecionado.getEmail());
                    binding.edtContato.setText(clienteSelecionado.getContato());
                    // Não preencher a senha. Deixar em branco para "não alterar" ou pedir nova.
                    binding.edtSenha.setHint("Nova Senha (deixe em branco para não alterar)");
                    binding.fieldsGroup.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void atualizarDadosCliente() {
        if (clienteSelecionado == null) {
            Toast.makeText(this, "Nenhum cliente selecionado para atualizar. Busque primeiro!", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = binding.edtNome.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String contato = binding.edtContato.getText().toString().trim();
        String senhaNova = binding.edtSenha.getText().toString().trim(); // Nova senha, pode ser vazia

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(contato)) {
            Toast.makeText(this, "Nome, e-mail e contato são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.setError("E-mail inválido!");
            return;
        }
        binding.edtEmail.setError(null);

        binding.progressBarAtualizar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            // Verificar se o email foi alterado e se já existe
            if (!email.equals(clienteSelecionado.getEmail())) {
                Cliente clienteComNovoEmail = clienteDao.buscarPorEmail(email);
                if (clienteComNovoEmail != null) {
                    runOnUiThread(() -> {
                        binding.progressBarAtualizar.setVisibility(View.GONE);
                        Toast.makeText(this, "Este e-mail já está em uso por outro cliente!", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
            }

            clienteSelecionado.setNome(nome);
            clienteSelecionado.setEmail(email);
            clienteSelecionado.setContato(contato);

            if (!TextUtils.isEmpty(senhaNova)) {
                clienteSelecionado.setSenha(BCrypt.hashpw(senhaNova, BCrypt.gensalt()));
            }

            int sucesso = clienteDao.atualizar(clienteSelecionado);
            runOnUiThread(() -> {
                binding.progressBarAtualizar.setVisibility(View.GONE);
                if (sucesso > 0) {
                    Toast.makeText(this, "Cliente atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao atualizar cliente. Verifique os dados ou se o ID é válido.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}