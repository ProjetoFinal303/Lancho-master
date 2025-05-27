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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projetofinal.dao.EstoqueDao;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Estoque;
import projetofinal.models.Produto;

public class CadastrarProdutoActivity extends AppCompatActivity {

    private ActivityCadastrarProdutoBinding binding;
    private ProdutoDao produtoDao;
    private EstoqueDao estoqueDao;
    private ExecutorService executorService;
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
        estoqueDao = new EstoqueDao(this);
        executorService = Executors.newSingleThreadExecutor();

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
            Toast.makeText(this, "Formato de preço inválido. Use ponto ou vírgula como separador decimal (ex: 10.50 ou 10,50).", Toast.LENGTH_LONG).show();
            return;
        }

        binding.progressBarCadastrarProduto.setVisibility(View.VISIBLE);
        binding.btnSalvarProduto.setEnabled(false);

        executorService.execute(() -> {
            Produto novoProduto = new Produto();
            novoProduto.setNome(nome);
            novoProduto.setDescricao(descricao);
            novoProduto.setPreco(preco);

            long produtoIdLong = produtoDao.inserir(novoProduto);

            String toastMessage;
            boolean sucessoOperacao = false;

            if (produtoIdLong == -2) {
                toastMessage = getString(R.string.produto_ja_existe);
            } else if (produtoIdLong != -1) {
                int produtoId = (int) produtoIdLong;
                Estoque novoEstoque = new Estoque(produtoId, 0);
                long estoqueId = estoqueDao.inserirOuAtualizar(novoEstoque);
                if (estoqueId != -1) {
                    toastMessage = getString(R.string.produto_adicionado_sucesso) + " ID: " + produtoIdLong;
                    sucessoOperacao = true;
                } else {
                    toastMessage = "Produto cadastrado, mas falha ao inicializar estoque.";
                    Log.e(TAG, "Produto ID " + produtoIdLong + " cadastrado, mas falha ao criar entrada de estoque.");
                }
            } else {
                toastMessage = getString(R.string.falha_adicionar_produto);
            }

            final boolean finalSucesso = sucessoOperacao;
            final String finalToastMessage = toastMessage;
            runOnUiThread(() -> {
                binding.progressBarCadastrarProduto.setVisibility(View.GONE);
                binding.btnSalvarProduto.setEnabled(true);
                Toast.makeText(CadastrarProdutoActivity.this, finalToastMessage, Toast.LENGTH_LONG).show();
                if (finalSucesso) {
                    Log.d(TAG, "Produto e estoque inicializados. Sinalizando para atualizar listas.");
                    VisualizarProdutoActivity.setProdutosDesatualizados(true);
                    VisualizarEstoqueActivity.setEstoqueDesatualizado(true);
                    finish();
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
        binding = null;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}