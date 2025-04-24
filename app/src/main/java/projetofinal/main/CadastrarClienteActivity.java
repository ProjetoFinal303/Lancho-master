package projetofinal.main;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.databinding.ActivityCadastrarClienteBinding;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projetofinal.dao.ClienteDao;
import projetofinal.database.DatabaseHelper;
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

        clienteDao = DatabaseHelper.getInstance(this).clienteDao();

        binding.btnCadastrar.setOnClickListener(view -> {
            String nome = binding.edtNome.getText().toString().trim();
            String email = binding.edtEmail.getText().toString().trim();
            String contato = binding.edtContato.getText().toString().trim();
            String senha = binding.edtSenha.getText().toString().trim();

            if (nome.isEmpty() || email.isEmpty() || contato.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar se o e-mail já está cadastrado
            Cliente clienteExistente = clienteDao.buscarPorEmail(email);
            if (clienteExistente != null) {
                Toast.makeText(this, "Já existe um cliente com esse e-mail!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gerar hash da senha
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            // Inserção em thread separada
            executor.execute(() -> {
                Cliente novoCliente = new Cliente(nome, email, contato, senhaHash);
                clienteDao.inserir(novoCliente);

                runOnUiThread(() -> {
                    Toast.makeText(CadastrarClienteActivity.this, "Cliente cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha tela e volta
                });
            });
        });
    }
}
