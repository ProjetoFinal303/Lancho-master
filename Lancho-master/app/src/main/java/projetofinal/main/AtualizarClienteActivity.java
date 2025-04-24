package projetofinal.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;

import projetofinal.database.DatabaseHelper;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;
import com.example.projetofinal.R;

public class AtualizarClienteActivity extends AppCompatActivity {
    private EditText edtId, edtNome, edtContato;
    private ClienteDao clienteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atualizar_cliente);

        edtId = findViewById(R.id.edtId);
        edtNome = findViewById(R.id.edtNome);
        edtContato = findViewById(R.id.edtContato);
        Button btnAtualizar = findViewById(R.id.btnAtualizar);

        clienteDao = new ClienteDao(this);

        btnAtualizar.setOnClickListener(v -> {
            String idTexto = edtId.getText().toString().trim();
            if (idTexto.isEmpty()) {
                Toast.makeText(this, "Informe um ID válido!", Toast.LENGTH_SHORT).show();
                return;
            }

            int id = Integer.parseInt(idTexto);
            String nome = edtNome.getText().toString();
            String contato = edtContato.getText().toString();

            AsyncTask.execute(() -> {
                Cliente cliente = clienteDao.buscarPorId(id);
                if (cliente != null) {
                    cliente.setNome(nome);
                    cliente.setContato(contato);
                    clienteDao.atualizar(cliente);
                    runOnUiThread(() -> Toast.makeText(this, "Cliente atualizado!", Toast.LENGTH_SHORT).show());
                    finish();
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Cliente não encontrado!", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}
