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

public class CadastrarClienteActivity extends AppCompatActivity {

    private EditText edtIdCliente, edtNome, edtContato;
    private Button btnCadastrar;
    private ClienteDao clienteDao;

    // Executor para tarefas em background
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_cliente);

        // Inicializa os componentes
        edtIdCliente = findViewById(R.id.edtIdCliente);
        edtNome = findViewById(R.id.edtNome);
        edtContato = findViewById(R.id.edtContato);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        // Inicializa o banco de dados
        try {
            clienteDao = DatabaseHelper.getInstance(this).clienteDao();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao acessar o banco de dados!", Toast.LENGTH_LONG).show();
            return;
        }

        // Botão Cadastrar
        btnCadastrar.setOnClickListener(view -> {
            String idTexto = edtIdCliente.getText().toString().trim();
            String nome = edtNome.getText().toString().trim();
            String contato = edtContato.getText().toString().trim();

            // Verifica se os campos estão preenchidos corretamente
            if (idTexto.isEmpty() || nome.isEmpty() || contato.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica se o ID é um número válido
            if (!idTexto.matches("\\d+")) {
                Toast.makeText(this, "Digite um ID válido!", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = Integer.parseInt(idTexto);

            // Executa a verificação e inserção no banco de dados
            executor.execute(() -> {
                boolean existe = clienteDao.clienteExiste(id); // Verifica se o cliente já existe

                if (existe) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erro: ID já cadastrado!", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    // Cria o cliente e insere no banco de dados
                    Cliente cliente = new Cliente(id, nome, contato);
                    clienteDao.inserir(cliente);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Cliente cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish(); // Volta para a tela anterior
                    });
                }
            });
        });
    }
}
