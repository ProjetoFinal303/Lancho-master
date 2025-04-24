package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.databinding.ActivityLoginBinding;

import org.mindrot.jbcrypt.BCrypt;

import projetofinal.dao.ClienteDao;
import projetofinal.database.DatabaseHelper;
import projetofinal.models.Cliente;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ClienteDao clienteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = DatabaseHelper.getInstance(this).clienteDao();

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            String senha = binding.edtSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Login ADM
            if (email.equals("admin@lanchonete.com") && senha.equals("admin123")) {
                startActivity(new Intent(this, TelaAdmActivity.class));
                finish();
                return;
            }

            // Login Cozinha
            if (email.equals("cozinha@lanchonete.com") && senha.equals("cozinha123")) {
                startActivity(new Intent(this, TelaCozinhaActivity.class));
                finish();
                return;
            }

            // Login Cliente (via email + senha hash)
            Cliente cliente = clienteDao.buscarPorEmail(email);
            if (cliente != null && BCrypt.checkpw(senha, cliente.getSenha())) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "E-mail ou senha incorretos!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnIrParaCadastro.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastrarClienteActivity.class));
        });
    }
}
