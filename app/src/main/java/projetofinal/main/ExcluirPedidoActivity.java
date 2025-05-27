package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityExcluirPedidoBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class ExcluirPedidoActivity extends AppCompatActivity {

    private ActivityExcluirPedidoBinding binding;
    private PedidoDao pedidoDao;
    private ExecutorService executorService;
    private Pedido pedidoParaExcluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar a Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarExcluirPedido); // Adicione este ID à sua Toolbar no XML
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.excluir_pedido_title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        pedidoDao = new PedidoDao(this);
        executorService = Executors.newSingleThreadExecutor();

        binding.btnConfirmarExcluirPedido.setOnClickListener(v -> confirmarEExcluirPedido());
    }

    private void confirmarEExcluirPedido() {
        String idPedidoStr = binding.edtPedidoIDExcluir.getText().toString().trim();
        if (TextUtils.isEmpty(idPedidoStr)) {
            Toast.makeText(this, "Informe o ID do Pedido.", Toast.LENGTH_SHORT).show();
            return;
        }

        int pedidoId;
        try {
            pedidoId = Integer.parseInt(idPedidoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID do Pedido inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarExcluirPedido.setVisibility(View.VISIBLE);
        binding.btnConfirmarExcluirPedido.setEnabled(false);

        executorService.execute(() -> {
            pedidoParaExcluir = pedidoDao.buscarPorId(pedidoId); // Supondo que PedidoDao tem buscarPorId

            runOnUiThread(() -> {
                binding.progressBarExcluirPedido.setVisibility(View.GONE);
                binding.btnConfirmarExcluirPedido.setEnabled(true);

                if (pedidoParaExcluir != null) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.confirmar_exclusao_pedido_message)
                            .setMessage(getString(R.string.confirmar_exclusao_pedido_message, pedidoParaExcluir.getId()))
                            .setPositiveButton(R.string.sim, (dialog, which) -> procederComExclusao(pedidoParaExcluir.getId()))
                            .setNegativeButton(R.string.nao, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Toast.makeText(ExcluirPedidoActivity.this, getString(R.string.pedido_nao_encontrado), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void procederComExclusao(int pedidoId) {
        binding.progressBarExcluirPedido.setVisibility(View.VISIBLE);
        binding.btnConfirmarExcluirPedido.setEnabled(false);

        executorService.execute(() -> {
            int linhasAfetadas = pedidoDao.excluir(pedidoId);
            runOnUiThread(() -> {
                binding.progressBarExcluirPedido.setVisibility(View.GONE);
                binding.btnConfirmarExcluirPedido.setEnabled(true);
                if (linhasAfetadas > 0) {
                    Toast.makeText(ExcluirPedidoActivity.this, getString(R.string.pedido_excluido_sucesso), Toast.LENGTH_SHORT).show();
                    binding.edtPedidoIDExcluir.setText(""); // Limpa o campo
                    // finish(); // Opcional: fechar a activity
                } else {
                    Toast.makeText(ExcluirPedidoActivity.this, getString(R.string.falha_excluir_pedido), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
