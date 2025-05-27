package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityCadastrarClienteBinding;
import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class CadastrarClienteActivity extends AppCompatActivity {

    private ActivityCadastrarClienteBinding binding;
    private ClienteDao clienteDao;
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastrarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);

        binding.btnCadastrar.setOnClickListener(view -> cadastrarCliente());
    }

    private void cadastrarCliente() {
        String nome = binding.edtNome.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String contato = binding.edtContato.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();
        String confirmarSenha = binding.edtConfirmarSenha.getText().toString().trim();


        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(contato) || TextUtils.isEmpty(senha) || TextUtils.isEmpty(confirmarSenha)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.setError("E-mail inválido!");
            Toast.makeText(this, "Formato de e-mail inválido!", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.edtEmail.setError(null);


        if (!senha.equals(confirmarSenha)) {
            binding.edtConfirmarSenha.setError("As senhas não coincidem!");
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.edtConfirmarSenha.setError(null);


        binding.progressBarCadastro.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            Cliente clienteExistente = clienteDao.buscarPorEmail(email);

            if (clienteExistente != null) {
                runOnUiThread(() -> {
                    binding.progressBarCadastro.setVisibility(View.GONE);
                    Toast.makeText(this, "Já existe um cliente com esse e-mail!", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
            Cliente novoCliente = new Cliente(nome, email, contato, senhaHash); // O ID é auto-gerado
            long clienteId = clienteDao.inserir(novoCliente);

            runOnUiThread(() -> {
                binding.progressBarCadastro.setVisibility(View.GONE);
                if (clienteId != -1) {
                    Toast.makeText(CadastrarClienteActivity.this, "Cliente cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CadastrarClienteActivity.this, "Erro ao cadastrar cliente. Tente novamente.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}