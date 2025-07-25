package projetofinal.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityExcluirProdutoBinding;
import projetofinal.dao.EstoqueDao;
import projetofinal.dao.ProdutoDao;

public class ExcluirProdutoActivity extends AppCompatActivity {
    private ActivityExcluirProdutoBinding binding;
    private ProdutoDao produtoDao;
    private EstoqueDao estoqueDao;
    private static final String TAG = "ExcluirProduto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        produtoDao = new ProdutoDao(this);
        estoqueDao = new EstoqueDao(this);

        binding.btnConfirmarExcluirProduto.setOnClickListener(v -> confirmarEExcluirProduto());
    }

    private void confirmarEExcluirProduto() {
        String idProdutoStr = binding.edtProdutoIDExcluir.getText().toString().trim();
        if (idProdutoStr.isEmpty()) { /* ... */ return; }

        int produtoId = Integer.parseInt(idProdutoStr);
        setLoading(true);

        produtoDao.buscarPorId(produtoId,
                produto -> runOnUiThread(() -> {
                    setLoading(false);
                    if (produto != null) {
                        new AlertDialog.Builder(this)
                                .setTitle("Confirmar Exclusão")
                                .setMessage("Tem certeza que deseja excluir o produto " + produto.getNome() + "?")
                                .setPositiveButton("Sim", (dialog, which) -> procederComExclusao(produto.getId()))
                                .setNegativeButton("Não", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Produto não encontrado.", Toast.LENGTH_LONG).show();
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void procederComExclusao(int produtoId) {
        setLoading(true);
        // Primeiro, exclui o estoque. Depois, o produto.
        estoqueDao.excluirPorProdutoId(produtoId,
                responseEstoque -> produtoDao.excluir(produtoId,
                        responseProduto -> runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(this, "Produto e estoque excluídos!", Toast.LENGTH_SHORT).show();
                            VisualizarProdutoActivity.setProdutosDesatualizados(true);
                            binding.edtProdutoIDExcluir.setText("");
                        }),
                        errorProduto -> runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(this, "Erro ao excluir produto: " + errorProduto.getMessage(), Toast.LENGTH_LONG).show();
                        })
                ),
                errorEstoque -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro ao excluir estoque: " + errorEstoque.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarExcluirProduto.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnConfirmarExcluirProduto.setEnabled(!isLoading);
    }
}