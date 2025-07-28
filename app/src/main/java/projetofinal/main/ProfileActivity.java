package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.databinding.ActivityProfileBinding;

import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private ClienteDao clienteDao;
    private int clienteIdLogado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarProfile;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        clienteDao = new ClienteDao(this);
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        binding.btnEditarDados.setOnClickListener(v -> {
            // Navega para a tela de Atualizar, passando o ID do cliente logado
            Intent intent = new Intent(ProfileActivity.this, AtualizarClienteActivity.class);
            intent.putExtra("CLIENTE_ID_EDITAR", clienteIdLogado);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDadosDoCliente();
    }

    private void carregarDadosDoCliente() {
        if (clienteIdLogado != -1) {
            clienteDao.buscarPorId(clienteIdLogado, cliente -> {
                if (cliente != null) {
                    runOnUiThread(() -> {
                        binding.textViewProfileNome.setText(cliente.getNome());
                        binding.textViewProfileEmail.setText(cliente.getEmail());
                        binding.textViewProfileContato.setText(cliente.getContato());
                    });
                }
            }, error -> runOnUiThread(() ->
                    Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show()
            ));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}