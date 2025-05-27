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
import com.example.projetofinal.databinding.ActivityExcluirProdutoBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Produto;
// Importar EstoqueDao se a exclusão de produto também precisar remover do estoque
import projetofinal.dao.EstoqueDao;


public class ExcluirProdutoActivity extends AppCompatActivity {

    private ActivityExcluirProdutoBinding binding;
    private ProdutoDao produtoDao;
    private EstoqueDao estoqueDao;
    private ExecutorService executorService;
    private Produto produtoParaExcluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarExcluirProduto;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.excluir_produto_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        produtoDao = new ProdutoDao(this);
        estoqueDao = new EstoqueDao(this); // Inicializar EstoqueDao
        executorService = Executors.newSingleThreadExecutor();

        binding.btnConfirmarExcluirProduto.setOnClickListener(v -> confirmarEExcluirProduto());
    }

    private void confirmarEExcluirProduto() {
        String idProdutoStr = binding.edtProdutoIDExcluir.getText().toString().trim();
        if (TextUtils.isEmpty(idProdutoStr)) {
            Toast.makeText(this, "Informe o ID do Produto.", Toast.LENGTH_SHORT).show();
            return;
        }

        int produtoId;
        try {
            produtoId = Integer.parseInt(idProdutoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID do Produto inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarExcluirProduto.setVisibility(View.VISIBLE);
        binding.btnConfirmarExcluirProduto.setEnabled(false);

        executorService.execute(() -> {
            produtoParaExcluir = produtoDao.buscarPorId(produtoId);

            runOnUiThread(() -> {
                binding.progressBarExcluirProduto.setVisibility(View.GONE);
                binding.btnConfirmarExcluirProduto.setEnabled(true);

                if (produtoParaExcluir != null) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.excluir_produto_title))
                            .setMessage(getString(R.string.confirmar_exclusao_produto_message, produtoParaExcluir.getNome(), produtoParaExcluir.getId()))
                            .setPositiveButton(R.string.sim, (dialog, which) -> procederComExclusao(produtoParaExcluir.getId()))
                            .setNegativeButton(R.string.nao, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    Toast.makeText(ExcluirProdutoActivity.this, getString(R.string.produto_nao_encontrado), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void procederComExclusao(int produtoId) {
        binding.progressBarExcluirProduto.setVisibility(View.VISIBLE);
        binding.btnConfirmarExcluirProduto.setEnabled(false);

        executorService.execute(() -> {
            estoqueDao.excluirPorProdutoId(produtoId);

            // Depois, exclui o produto
            int linhasAfetadas = produtoDao.excluir(produtoId);

            runOnUiThread(() -> {
                binding.progressBarExcluirProduto.setVisibility(View.GONE);
                binding.btnConfirmarExcluirProduto.setEnabled(true);
                if (linhasAfetadas > 0) {
                    Toast.makeText(ExcluirProdutoActivity.this, getString(R.string.produto_excluido_sucesso), Toast.LENGTH_SHORT).show();
                    binding.edtProdutoIDExcluir.setText(""); // Limpa o campo
                    // finish(); // Opcional: fechar a activity
                } else {
                    // Isso pode acontecer se o produto foi excluído entre a busca e a confirmação,
                    // ou se o ID era de um produto que não existia mais (embora a busca inicial devesse pegar isso).
                    Toast.makeText(ExcluirProdutoActivity.this, getString(R.string.falha_excluir_produto) + " (Produto pode já ter sido removido)", Toast.LENGTH_LONG).show();
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
