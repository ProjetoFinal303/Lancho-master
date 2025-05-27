package projetofinal.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityAtualizarEstoqueBinding;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Locale;
import android.util.Log;

import projetofinal.dao.EstoqueDao;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Estoque;
import projetofinal.models.Produto;

public class AtualizarEstoqueActivity extends AppCompatActivity {

    private ActivityAtualizarEstoqueBinding binding;
    private ProdutoDao produtoDao;
    private EstoqueDao estoqueDao;
    private ExecutorService executorService;
    private Produto produtoSelecionado;
    private Estoque estoqueAtualDoProduto;
    private int produtoIdRecebido = -1;
    private static final String TAG = "AtualizarEstoque";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtualizarEstoqueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarAtualizarEstoque;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.atualizar_estoque_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        produtoDao = new ProdutoDao(this);
        estoqueDao = new EstoqueDao(this);
        executorService = Executors.newSingleThreadExecutor();

        binding.btnBuscarProdutoEstoque.setOnClickListener(v -> buscarProdutoEseuEstoquePeloInput());
        binding.btnSalvarAtualizacaoEstoque.setOnClickListener(v -> salvarAtualizacaoEstoque());

        if (getIntent().hasExtra("PRODUTO_ID_ESTOQUE")) {
            produtoIdRecebido = getIntent().getIntExtra("PRODUTO_ID_ESTOQUE", -1);
            Log.d(TAG, "ID do produto recebido via Intent: " + produtoIdRecebido);
            if (produtoIdRecebido != -1) {
                binding.edtIdProdutoEstoque.setText(String.valueOf(produtoIdRecebido));
                binding.edtIdProdutoEstoque.setEnabled(false);
                binding.btnBuscarProdutoEstoque.setVisibility(View.GONE);
                buscarProdutoEseuEstoque(produtoIdRecebido);
            }
        }
    }

    private void buscarProdutoEseuEstoquePeloInput() {
        String idProdutoStr = binding.edtIdProdutoEstoque.getText().toString().trim();
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
        buscarProdutoEseuEstoque(produtoId);
    }

    private void buscarProdutoEseuEstoque(int produtoIdParaBuscar) {
        if (produtoIdParaBuscar == -1) {
            Log.w(TAG, "buscarProdutoEseuEstoque chamado com ID -1, retornando.");
            return;
        }
        Log.d(TAG, "Buscando produto e estoque para ID: " + produtoIdParaBuscar);

        binding.progressBarAtualizarEstoque.setVisibility(View.VISIBLE);
        binding.textViewNomeProdutoEstoqueInfo.setVisibility(View.GONE);
        binding.textViewQtdAtualEstoqueInfo.setVisibility(View.GONE);
        binding.tilNovaQuantidadeEstoque.setVisibility(View.GONE);
        binding.btnSalvarAtualizacaoEstoque.setVisibility(View.GONE);

        executorService.execute(() -> {
            produtoSelecionado = produtoDao.buscarPorId(produtoIdParaBuscar);
            if (produtoSelecionado != null) {
                estoqueAtualDoProduto = estoqueDao.buscarPorProdutoId(produtoIdParaBuscar);
                Log.d(TAG, "Produto encontrado: " + produtoSelecionado.getNome() + ". Estoque: " + (estoqueAtualDoProduto != null ? estoqueAtualDoProduto.getQuantidade() : "Nenhum"));
            } else {
                Log.w(TAG, "Produto não encontrado para ID: " + produtoIdParaBuscar);
            }

            runOnUiThread(() -> {
                binding.progressBarAtualizarEstoque.setVisibility(View.GONE);
                if (produtoSelecionado != null) {
                    binding.textViewNomeProdutoEstoqueInfo.setText(getString(R.string.produto_nome_estoque, produtoSelecionado.getNome()));
                    binding.textViewNomeProdutoEstoqueInfo.setVisibility(View.VISIBLE);

                    if (estoqueAtualDoProduto != null) {
                        binding.textViewQtdAtualEstoqueInfo.setText(getString(R.string.quantidade_em_estoque, estoqueAtualDoProduto.getQuantidade()));
                        binding.edtNovaQuantidadeEstoque.setText(String.valueOf(estoqueAtualDoProduto.getQuantidade()));
                    } else {
                        binding.textViewQtdAtualEstoqueInfo.setText(getString(R.string.quantidade_em_estoque, 0));
                        binding.edtNovaQuantidadeEstoque.setText("0");
                    }
                    binding.textViewQtdAtualEstoqueInfo.setVisibility(View.VISIBLE);
                    binding.tilNovaQuantidadeEstoque.setVisibility(View.VISIBLE);
                    binding.btnSalvarAtualizacaoEstoque.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AtualizarEstoqueActivity.this, getString(R.string.produto_nao_encontrado_estoque), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void salvarAtualizacaoEstoque() {
        if (produtoSelecionado == null) {
            Toast.makeText(this, "Nenhum produto selecionado. Busque um produto primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        String novaQuantidadeStr = binding.edtNovaQuantidadeEstoque.getText().toString().trim();
        if (TextUtils.isEmpty(novaQuantidadeStr)) {
            Toast.makeText(this, "Informe a nova quantidade.", Toast.LENGTH_SHORT).show();
            return;
        }

        int novaQuantidade;
        try {
            novaQuantidade = Integer.parseInt(novaQuantidadeStr);
            if (novaQuantidade < 0) {
                Toast.makeText(this, "A quantidade não pode ser negativa.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Salvando atualização de estoque para Produto ID: " + produtoSelecionado.getId() + ", Nova Qtd: " + novaQuantidade);
        binding.progressBarAtualizarEstoque.setVisibility(View.VISIBLE);
        binding.btnSalvarAtualizacaoEstoque.setEnabled(false);
        if (binding.edtIdProdutoEstoque.isEnabled()){ // Só desabilita se o botão de busca estiver visível/ativo
            binding.btnBuscarProdutoEstoque.setEnabled(false);
        }


        executorService.execute(() -> {
            Estoque estoqueParaSalvar;
            if (estoqueAtualDoProduto != null) {
                estoqueParaSalvar = estoqueAtualDoProduto;
                estoqueParaSalvar.setQuantidade(novaQuantidade);
            } else {
                estoqueParaSalvar = new Estoque(produtoSelecionado.getId(), novaQuantidade);
            }

            long resultado = estoqueDao.inserirOuAtualizar(estoqueParaSalvar);

            runOnUiThread(() -> {
                binding.progressBarAtualizarEstoque.setVisibility(View.GONE);
                binding.btnSalvarAtualizacaoEstoque.setEnabled(true);
                if (binding.edtIdProdutoEstoque.isEnabled()){
                    binding.btnBuscarProdutoEstoque.setEnabled(true);
                }

                if (resultado != -1) {
                    Toast.makeText(AtualizarEstoqueActivity.this, getString(R.string.estoque_atualizado_sucesso), Toast.LENGTH_SHORT).show();
                    binding.textViewQtdAtualEstoqueInfo.setText(getString(R.string.quantidade_em_estoque, novaQuantidade));

                    // Atualiza o objeto local para refletir a mudança
                    if (estoqueAtualDoProduto != null) {
                        estoqueAtualDoProduto.setQuantidade(novaQuantidade);
                    } else {
                        // Se era uma nova entrada de estoque, o ID do estoque foi retornado em 'resultado'
                        // e o produtoId já é conhecido.
                        estoqueAtualDoProduto = new Estoque((int)resultado, produtoSelecionado.getId(), novaQuantidade);
                    }
                    Log.d(TAG, "Estoque atualizado. Sinalizando VisualizarEstoqueActivity.");
                    VisualizarEstoqueActivity.setEstoqueDesatualizado(true); // CORRETO

                    if (produtoIdRecebido != -1) {
                        finish();
                    }
                } else {
                    Toast.makeText(AtualizarEstoqueActivity.this, getString(R.string.falha_atualizar_estoque), Toast.LENGTH_LONG).show();
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
