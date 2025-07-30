package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.projetofinal.databinding.ActivityAtualizarClienteBinding;
import org.json.JSONObject;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class AtualizarClienteActivity extends BaseActivity {

    private ActivityAtualizarClienteBinding binding;
    private ClienteDao clienteDao;
    private Cliente clienteSelecionado;
    private static final String TAG = "AtualizarCliente";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtualizarClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);

        if (getIntent().hasExtra("CLIENTE_ID_EDITAR")) {
            int clienteId = getIntent().getIntExtra("CLIENTE_ID_EDITAR", -1);
            if (clienteId != -1) {
                binding.tilIdClienteAtualizar.setVisibility(View.GONE);
                binding.btnBuscar.setVisibility(View.GONE);
                buscarClienteParaAtualizar(clienteId);
            }
        } else {
            binding.tilIdClienteAtualizar.setVisibility(View.VISIBLE);
            binding.btnBuscar.setVisibility(View.VISIBLE);
        }

        binding.btnBuscar.setOnClickListener(v -> {
            String idStr = binding.edtId.getText().toString().trim();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(this, "Informe o ID!", Toast.LENGTH_SHORT).show();
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
        setLoading(true, false);
        clienteDao.buscarPorId(id,
                cliente -> runOnUiThread(() -> {
                    setLoading(false, cliente != null);
                    if (cliente != null) {
                        clienteSelecionado = cliente;
                        binding.edtNome.setText(cliente.getNome());
                        binding.edtEmail.setText(cliente.getEmail());
                        binding.edtContato.setText(cliente.getContato());
                        binding.edtSenha.setText("");
                    } else {
                        Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false, false);
                    Log.e(TAG, "Erro ao buscar cliente: ", error);
                    Toast.makeText(this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void atualizarDadosCliente() {
        if (clienteSelecionado == null) {
            Toast.makeText(this, "Busque um cliente primeiro!", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = binding.edtNome.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String contato = binding.edtContato.getText().toString().trim();
        String senhaNova = binding.edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(contato)) {
            Toast.makeText(this, "Nome, e-mail e contato são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true, true);

        try {
            JSONObject payload = new JSONObject();
            payload.put("nome", nome);
            payload.put("email", email);
            payload.put("contato", contato);
            if (!senhaNova.isEmpty()) {
                payload.put("senha", senhaNova);
            }

            clienteDao.atualizar(clienteSelecionado.getId(), payload,
                    response -> runOnUiThread(() -> {
                        setLoading(false, true);
                        Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    }),
                    error -> runOnUiThread(() -> {
                        setLoading(false, true);
                        Log.e(TAG, "Erro ao atualizar cliente: ", error);
                        Toast.makeText(this, "Erro ao atualizar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    })
            );
        } catch (Exception e) {
            setLoading(false, true);
            Toast.makeText(this, "Erro ao preparar dados.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean isLoading, boolean showFields) {
        binding.progressBarAtualizar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.fieldsGroup.setVisibility(showFields ? View.VISIBLE : View.GONE);
        binding.btnBuscar.setEnabled(!isLoading);
        binding.btnAtualizar.setEnabled(!isLoading && showFields);
    }
}