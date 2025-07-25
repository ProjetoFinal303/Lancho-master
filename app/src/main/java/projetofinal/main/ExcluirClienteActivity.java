package projetofinal.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityExcluirClienteBinding;
import projetofinal.dao.ClienteDao;

public class ExcluirClienteActivity extends AppCompatActivity {

    private ActivityExcluirClienteBinding binding;
    private ClienteDao clienteDao;
    private static final String TAG = "ExcluirCliente";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteDao = new ClienteDao(this);
        binding.btnExcluir.setOnClickListener(v -> {
            String idString = binding.edtId.getText().toString().trim();
            if (idString.isEmpty()) {
                Toast.makeText(this, "Digite um ID válido!", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            int id = Integer.parseInt(idString);

            clienteDao.excluir(id,
                    response -> runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(this, "Cliente excluído!", Toast.LENGTH_SHORT).show();
                        finish();
                    }),
                    error -> runOnUiThread(() -> {
                        setLoading(false);
                        Log.e(TAG, "Erro ao excluir cliente: ", error);
                        Toast.makeText(this, "Erro ao excluir: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    })
            );
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarExcluir.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnExcluir.setEnabled(!isLoading);
    }
}