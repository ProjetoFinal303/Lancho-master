package projetofinal.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.R;
import projetofinal.database.DatabaseHelper;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExcluirClienteActivity extends AppCompatActivity {
    private EditText edtId;
    private ClienteDao clienteDao;

    // Executor para rodar tarefas em background
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluir_cliente);

        edtId = findViewById(R.id.edtId);
        Button btnExcluir = findViewById(R.id.btnExcluir);

        // Inicializando o banco de dados
        try {
            clienteDao = DatabaseHelper.getInstance(this).clienteDao();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao acessar o banco de dados!", Toast.LENGTH_LONG).show();
            return;
        }

        btnExcluir.setOnClickListener(v -> {
            String idTexto = edtId.getText().toString().trim();

            // Verifica se o ID contém apenas números
            if (!idTexto.matches("\\d+")) {
                Toast.makeText(this, "Digite um ID válido!", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = Integer.parseInt(idTexto);

            // Executa a exclusão do cliente em uma thread separada
            executor.execute(() -> {
                Cliente cliente = clienteDao.buscarPorId(id);

                runOnUiThread(() -> {
                    if (cliente != null) {
                        clienteDao.excluir(cliente.getId());
                        Toast.makeText(this, "Cliente excluído!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }
}
