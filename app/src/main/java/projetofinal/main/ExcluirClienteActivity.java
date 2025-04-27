package projetofinal.main;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.databinding.ActivityExcluirClienteBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class ExcluirClienteActivity extends AppCompatActivity {

    private ActivityExcluirClienteBinding binding;
    private ClienteDao clienteDao;

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializa o banco de dados
        try {
            clienteDao = new ClienteDao(this); // Passando o contexto para o ClienteDao
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao acessar o banco de dados!", Toast.LENGTH_LONG).show();
            return;
        }

        // Botão Excluir
        binding.btnExcluir.setOnClickListener(v -> {
            // Agora estamos usando o ID para buscar o cliente
            String idString = binding.edtId.getText().toString().trim();

            if (idString.isEmpty()) {
                Toast.makeText(this, "Digite um ID válido!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int id = Integer.parseInt(idString); // Converte o ID para inteiro

                executor.execute(() -> {
                    // Buscar cliente usando o ID
                    Cliente cliente = clienteDao.buscarPorId(id);

                    runOnUiThread(() -> {
                        if (cliente != null) {
                            // Excluir cliente encontrado
                            clienteDao.excluir(cliente.getId());
                            Toast.makeText(this, "Cliente excluído!", Toast.LENGTH_SHORT).show();
                            finish();  // Finaliza a atividade
                        } else {
                            // Caso o cliente não seja encontrado
                            Toast.makeText(this, "Cliente não encontrado com esse ID!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (NumberFormatException e) {
                // Caso o ID não seja um número válido
                Toast.makeText(this, "ID inválido! Insira um número válido.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
