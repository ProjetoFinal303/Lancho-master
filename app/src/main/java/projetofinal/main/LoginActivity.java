package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.databinding.ActivityLoginBinding;

import org.mindrot.jbcrypt.BCrypt;

import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ClienteDao clienteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this); // Passando o contexto para o ClienteDao


        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            String senha = binding.edtSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Login ADM
            if (email.equals("admin@lanchonete.com") && senha.equals("admin123")) {
                // Redireciona para a tela principal do administrador
                Intent intent = new Intent(LoginActivity.this, MainAdminActivity.class);
                startActivity(intent);
                finish();  // Finaliza a LoginActivity
                return;
            }

            // Login Cozinha
            if (email.equals("cozinha@lanchonete.com") && senha.equals("cozinha123")) {
               Intent intent = new Intent(LoginActivity.this, MainCozinhaActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Login Cliente (via email + senha hash)
            Cliente cliente = clienteDao.buscarPorEmail(email);
            if (cliente != null) {
                if (BCrypt.checkpw(senha, cliente.getSenha())) {
                    // Redireciona o cliente para a MainClienteActivity após login bem-sucedido
                    Intent intent = new Intent(LoginActivity.this, MainClienteActivity.class);
                    startActivity(intent);
                    finish();  // Finaliza a LoginActivity
                } else {
                    Toast.makeText(this, "E-mail ou senha incorretos!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Cliente não encontrado com o email: " + email);
            }
        });

        binding.btnIrParaCadastro.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastrarClienteActivity.class));
        });
    }
}
