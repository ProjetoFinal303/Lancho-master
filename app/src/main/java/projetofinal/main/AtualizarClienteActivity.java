package projetofinal.main;

import android.os.Bundle;
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

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtualizarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this); // Passando o contexto para o ClienteDao

        // Botão BUSCAR CLIENTE
        binding.btnBuscar.setOnClickListener(v -> {
            String idStr = binding.edtId.getText().toString().trim();
            if (idStr.isEmpty()) {
                Toast.makeText(this, "Informe o ID do cliente!", Toast.LENGTH_SHORT).show();
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "ID inválido!", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                clienteSelecionado = clienteDao.buscarPorId(id);

                runOnUiThread(() -> {
                    if (clienteSelecionado == null) {
                        Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
                    } else {
                        binding.edtNome.setText(clienteSelecionado.getNome());
                        binding.edtContato.setText(clienteSelecionado.getContato());
                        binding.edtEmail.setText(clienteSelecionado.getEmail());
                    }
                });
            });
        });

        // Botão ATUALIZAR
        binding.btnAtualizar.setOnClickListener(v -> {
            if (clienteSelecionado == null) {
                Toast.makeText(this, "Busque um cliente antes de atualizar!", Toast.LENGTH_SHORT).show();
                return;
            }

            String nome = binding.edtNome.getText().toString().trim();
            String contato = binding.edtContato.getText().toString().trim();
            String email = binding.edtEmail.getText().toString().trim();
            String senha = binding.edtSenha.getText().toString().trim();

            if (nome.isEmpty() || contato.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Todos os campos são obrigatórios (exceto senha)!", Toast.LENGTH_SHORT).show();
                return;
            }

            String senhaHash = senha.isEmpty() ? clienteSelecionado.getSenha() : BCrypt.hashpw(senha, BCrypt.gensalt());
            Cliente clienteAtualizado = new Cliente(clienteSelecionado.getId(), nome, email, contato, senhaHash);

            executor.execute(() -> {
                boolean sucesso = clienteDao.atualizar(clienteAtualizado) > 0;
                runOnUiThread(() -> {
                    if (sucesso) {
                        Toast.makeText(this, "Cliente atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao atualizar cliente.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }
}
