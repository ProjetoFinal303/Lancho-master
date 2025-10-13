package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.projetofinal.R;
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

        // Ação para o novo botão de voltar no topo
        binding.btnVoltar.setOnClickListener(v -> finish());

        // Ação para o botão "Já tem uma conta?" na parte inferior
        binding.btnIrParaLogin.setOnClickListener(v -> finish());
    }

    private void cadastrarCliente() {
        String nome = binding.edtNome.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String contato = binding.edtContato.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();
        String confirmarSenha = binding.edtConfirmarSenha.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, R.string.campos_obrigatorios, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, R.string.senhas_nao_coincidem, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.email_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Cliente novoCliente = new Cliente(nome, email, contato, senha);
        novoCliente.setRole("cliente");

        clienteDao.inserir(novoCliente,
                response -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(CadastrarClienteActivity.this, R.string.cadastro_sucesso, Toast.LENGTH_SHORT).show();
                    finish();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    if (error.getMessage() != null && error.getMessage().contains("duplicate key")) {
                        Toast.makeText(CadastrarClienteActivity.this, R.string.email_ja_existe, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CadastrarClienteActivity.this, getString(R.string.cadastro_falha) + ": " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarCadastro.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnCadastrar.setEnabled(!isLoading);
        binding.btnIrParaLogin.setEnabled(!isLoading);
        binding.btnVoltar.setEnabled(!isLoading);
    }
}
