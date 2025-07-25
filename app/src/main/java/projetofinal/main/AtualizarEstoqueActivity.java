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
import com.example.projetofinal.databinding.ActivityAtualizarEstoqueBinding;
import projetofinal.dao.EstoqueDao;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Estoque;
import projetofinal.models.Produto;

public class AtualizarEstoqueActivity extends AppCompatActivity {

    private ActivityAtualizarEstoqueBinding binding;
    private ProdutoDao produtoDao;
    private EstoqueDao estoqueDao;
    private Produto produtoSelecionado;
    private Estoque estoqueAtualDoProduto;
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
        }

        produtoDao = new ProdutoDao(this);
        estoqueDao = new EstoqueDao(this);

        binding.btnBuscarProdutoEstoque.setOnClickListener(v -> buscarProdutoEseuEstoquePeloInput());
        binding.btnSalvarAtualizacaoEstoque.setOnClickListener(v -> salvarAtualizacaoEstoque());
    }

    private void buscarProdutoEseuEstoquePeloInput() {
        String idProdutoStr = binding.edtIdProdutoEstoque.getText().toString().trim();
        if (TextUtils.isEmpty(idProdutoStr)) {
            Toast.makeText(this, "Informe o ID do Produto.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int produtoId = Integer.parseInt(idProdutoStr);
            buscarProdutoEseuEstoque(produtoId);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID do Produto inválido.", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarProdutoEseuEstoque(int produtoId) {
        setLoading(true, false);

        produtoDao.buscarPorId(produtoId,
                produto -> {
                    if (produto == null) {
                        runOnUiThread(() -> {
                            setLoading(false, false);
                            Toast.makeText(this, "Produto não encontrado.", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    produtoSelecionado = produto;

                    // Agora que achou o produto, busca o estoque dele
                    estoqueDao.buscarPorProdutoId(produtoId,
                            estoque -> runOnUiThread(() -> {
                                setLoading(false, true);
                                estoqueAtualDoProduto = estoque;
                                binding.textViewNomeProdutoEstoqueInfo.setText("Produto: " + produto.getNome());
                                if (estoque != null) {
                                    binding.textViewQtdAtualEstoqueInfo.setText("Quantidade Atual: " + estoque.getQuantidade());
                                    binding.edtNovaQuantidadeEstoque.setText(String.valueOf(estoque.getQuantidade()));
                                } else {
                                    binding.textViewQtdAtualEstoqueInfo.setText("Quantidade Atual: 0");
                                    binding.edtNovaQuantidadeEstoque.setText("0");
                                }
                            }),
                            error -> runOnUiThread(() -> {
                                setLoading(false, true);
                                // Erro ao buscar estoque, mas o produto foi encontrado
                                binding.textViewNomeProdutoEstoqueInfo.setText("Produto: " + produto.getNome());
                                binding.textViewQtdAtualEstoqueInfo.setText("Erro ao carregar quantidade.");
                            })
                    );
                },
                error -> runOnUiThread(() -> {
                    setLoading(false, false);
                    Log.e(TAG, "Erro ao buscar produto: ", error);
                    Toast.makeText(this, "Erro de conexão ao buscar produto.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void salvarAtualizacaoEstoque() {
        if (produtoSelecionado == null) {
            Toast.makeText(this, "Nenhum produto selecionado.", Toast.LENGTH_SHORT).show();
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
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true, true);

        Estoque estoqueParaSalvar = new Estoque(produtoSelecionado.getId(), novaQuantidade);

        estoqueDao.inserirOuAtualizar(estoqueParaSalvar,
                response -> runOnUiThread(() -> {
                    setLoading(false, true);
                    Toast.makeText(this, "Estoque atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    VisualizarEstoqueActivity.setEstoqueDesatualizado(true);
                    finish();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false, true);
                    Log.e(TAG, "Erro ao salvar estoque: ", error);
                    Toast.makeText(this, "Erro ao salvar estoque: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void setLoading(boolean isLoading, boolean showFields) {
        binding.progressBarAtualizarEstoque.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.textViewNomeProdutoEstoqueInfo.setVisibility(showFields ? View.VISIBLE : View.GONE);
        binding.textViewQtdAtualEstoqueInfo.setVisibility(showFields ? View.VISIBLE : View.GONE);
        binding.tilNovaQuantidadeEstoque.setVisibility(showFields ? View.VISIBLE : View.GONE);
        binding.btnSalvarAtualizacaoEstoque.setVisibility(showFields ? View.VISIBLE : View.GONE);
        binding.btnBuscarProdutoEstoque.setEnabled(!isLoading);
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