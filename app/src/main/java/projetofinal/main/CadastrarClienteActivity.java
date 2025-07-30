package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.projetofinal.databinding.ActivityCadastrarClienteBinding;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class CadastrarClienteActivity extends BaseActivity {

    private ActivityCadastrarClienteBinding binding;
    private ClienteDao clienteDao;

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
        String senha = binding.edtSenha.getText().toString().trim();
        String confirmarSenha = binding.edtConfirmarSenha.getText().toString().trim();
        // O campo contato não está no layout de cadastro, então passamos vazio
        String contato = "";

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Cliente novoCliente = new Cliente(nome, email, contato, senha);

        clienteDao.inserir(novoCliente,
                response -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(CadastrarClienteActivity.this, "Cliente cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(CadastrarClienteActivity.this, "Erro ao cadastrar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarCadastro.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnCadastrar.setEnabled(!isLoading);
    }
}