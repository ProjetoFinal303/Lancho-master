package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityExcluirPedidoBinding;
import projetofinal.dao.PedidoDao;

public class ExcluirPedidoActivity extends AppCompatActivity {

    private ActivityExcluirPedidoBinding binding;
    private PedidoDao pedidoDao;
    private static final String TAG = "ExcluirPedido";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarExcluirPedido;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.excluir_pedido_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pedidoDao = new PedidoDao(this);
        binding.btnConfirmarExcluirPedido.setOnClickListener(v -> confirmarEExcluirPedido());
    }

    private void confirmarEExcluirPedido() {
        String idPedidoStr = binding.edtPedidoIDExcluir.getText().toString().trim();
        if (TextUtils.isEmpty(idPedidoStr)) {
            Toast.makeText(this, "Informe o ID do Pedido.", Toast.LENGTH_SHORT).show();
            return;
        }
        int pedidoId = Integer.parseInt(idPedidoStr);

        setLoading(true);
        pedidoDao.buscarPorId(pedidoId,
                pedido -> runOnUiThread(() -> {
                    setLoading(false);
                    if (pedido != null) {
                        new AlertDialog.Builder(this)
                                .setTitle("Confirmar Exclusão")
                                .setMessage("Tem certeza que deseja excluir o pedido #" + pedido.getId() + "?")
                                .setPositiveButton("Sim", (dialog, which) -> procederComExclusao(pedido.getId()))
                                .setNegativeButton("Não", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Pedido não encontrado.", Toast.LENGTH_LONG).show();
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro ao buscar pedido: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void procederComExclusao(int pedidoId) {
        setLoading(true);
        pedidoDao.excluir(pedidoId,
                response -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Pedido excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    binding.edtPedidoIDExcluir.setText("");
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Falha ao excluir pedido: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarExcluirPedido.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnConfirmarExcluirPedido.setEnabled(!isLoading);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}