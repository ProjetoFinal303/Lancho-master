package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityCadastrarProdutoBinding;
import java.math.BigDecimal;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Produto;

public class CadastrarProdutoActivity extends AppCompatActivity {

    private ActivityCadastrarProdutoBinding binding;
    private ProdutoDao produtoDao;
    private static final String TAG = "CadastrarProduto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastrarProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarCadastrarProduto;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.cadastrar_produto_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        produtoDao = new ProdutoDao(this);

        binding.btnSalvarProduto.setOnClickListener(v -> cadastrarNovoProduto());
    }

    private void cadastrarNovoProduto() {
        String nome = binding.edtNomeProduto.getText().toString().trim();
        String descricao = binding.edtDescricaoProduto.getText().toString().trim();
        String precoStr = binding.edtPrecoProduto.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(descricao) || TextUtils.isEmpty(precoStr)) {
            Toast.makeText(this, getString(R.string.campos_obrigatorios), Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal preco;
        try {
            String normalizedPrecoStr = precoStr.replace(",", ".");
            preco = new BigDecimal(normalizedPrecoStr);
            if (preco.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(this, "O preço deve ser um valor positivo.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de preço inválido.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        Produto novoProduto = new Produto();
        novoProduto.setNome(nome);
        novoProduto.setDescricao(descricao);
        novoProduto.setPreco(preco);

        produtoDao.inserir(novoProduto,
                // Callback de Sucesso
                produtoCriado -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(CadastrarProdutoActivity.this, "Produto cadastrado com sucesso!", Toast.LENGTH_LONG).show();
                    // Avisa as outras telas que a lista de produtos mudou
                    VisualizarProdutoActivity.setProdutosDesatualizados(true);
                    VisualizarEstoqueActivity.setEstoqueDesatualizado(true);
                    finish();
                }),
                // Callback de Erro
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao cadastrar produto: ", error);
                    Toast.makeText(CadastrarProdutoActivity.this, "Falha ao cadastrar produto: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarCadastrarProduto.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSalvarProduto.setEnabled(!isLoading);
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