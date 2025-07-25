package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityAtualizarProdutoBinding;
import java.math.BigDecimal;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Produto;

public class AtualizarProdutoActivity extends AppCompatActivity {

    private ActivityAtualizarProdutoBinding binding;
    private ProdutoDao produtoDao;
    private Produto produtoSelecionado;
    private static final String TAG = "AtualizarProduto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtualizarProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarAtualizarProduto;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.atualizar_produto_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        produtoDao = new ProdutoDao(this);
        binding.btnBuscarProdutoAtualizar.setOnClickListener(v -> buscarProduto());
        binding.btnSalvarAtualizacaoProduto.setOnClickListener(v -> salvarAtualizacoes());
    }

    private void buscarProduto() {
        String idStr = binding.edtIdProdutoAtualizar.getText().toString().trim();
        if(idStr.isEmpty()){ Toast.makeText(this, "Informe o ID do produto", Toast.LENGTH_SHORT).show(); return; }

        setLoading(true, false);
        int id = Integer.parseInt(idStr);

        produtoDao.buscarPorId(id,
                produto -> runOnUiThread(() -> {
                    setLoading(false, produto != null);
                    if (produto != null) {
                        produtoSelecionado = produto;
                        binding.edtNomeProdutoAtualizar.setText(produto.getNome());
                        binding.edtDescricaoProdutoAtualizar.setText(produto.getDescricao());
                        binding.edtPrecoProdutoAtualizar.setText(produto.getPreco().toPlainString());
                    } else {
                        Toast.makeText(this, "Produto não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false, false);
                    Log.e(TAG, "Erro ao buscar produto: ", error);
                    Toast.makeText(this, "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void salvarAtualizacoes() {
        if (produtoSelecionado == null) { Toast.makeText(this, "Busque um produto primeiro.", Toast.LENGTH_SHORT).show(); return; }

        String nome = binding.edtNomeProdutoAtualizar.getText().toString().trim();
        String descricao = binding.edtDescricaoProdutoAtualizar.getText().toString().trim();
        String precoStr = binding.edtPrecoProdutoAtualizar.getText().toString().trim();

        if (nome.isEmpty() || descricao.isEmpty() || precoStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show(); return;
        }

        setLoading(true, true);

        produtoSelecionado.setNome(nome);
        produtoSelecionado.setDescricao(descricao);
        produtoSelecionado.setPreco(new BigDecimal(precoStr.replace(",", ".")));

        produtoDao.atualizar(produtoSelecionado,
                response -> runOnUiThread(() -> {
                    setLoading(false, true);
                    Toast.makeText(this, "Produto atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    VisualizarProdutoActivity.setProdutosDesatualizados(true);
                    finish();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false, true);
                    Log.e(TAG, "Erro ao atualizar produto: ", error);
                    Toast.makeText(this, "Erro ao atualizar: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void setLoading(boolean isLoading, boolean showFields) {
        binding.progressBarAtualizarProduto.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.fieldsGroupProduto.setVisibility(showFields ? View.VISIBLE : View.GONE);
        binding.btnBuscarProdutoAtualizar.setEnabled(!isLoading);
        binding.btnSalvarAtualizacaoProduto.setEnabled(!isLoading && showFields);
    }
}