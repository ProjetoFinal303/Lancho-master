package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityAtualizarProdutoBinding;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Locale;

import projetofinal.dao.ProdutoDao;
import projetofinal.models.Produto;

public class AtualizarProdutoActivity extends AppCompatActivity {

    private ActivityAtualizarProdutoBinding binding;
    private ProdutoDao produtoDao;
    private ExecutorService executorService;
    private Produto produtoSelecionado;
    private int produtoIdParaCarregar = -1;

    public static final String EXTRA_PRODUTO_ID = "projetofinal.main.EXTRA_PRODUTO_ID";

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
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        produtoDao = new ProdutoDao(this);
        executorService = Executors.newSingleThreadExecutor();

        binding.btnBuscarProdutoAtualizar.setOnClickListener(v -> buscarProdutoParaAtualizarOnClick());
        binding.btnSalvarAtualizacaoProduto.setOnClickListener(v -> salvarAtualizacoesDoProduto());

        if (getIntent().hasExtra(EXTRA_PRODUTO_ID)) {
            produtoIdParaCarregar = getIntent().getIntExtra(EXTRA_PRODUTO_ID, -1);
            if (produtoIdParaCarregar != -1) {
                binding.edtIdProdutoAtualizar.setText(String.valueOf(produtoIdParaCarregar));
                binding.edtIdProdutoAtualizar.setEnabled(false);
                binding.btnBuscarProdutoAtualizar.setVisibility(View.GONE);
                buscarProdutoParaAtualizar(produtoIdParaCarregar);
            }
        }
    }

    private void buscarProdutoParaAtualizarOnClick() {
        String idStr = binding.edtIdProdutoAtualizar.getText().toString().trim();
        if (TextUtils.isEmpty(idStr)) {
            Toast.makeText(this, getString(R.string.informe_id_generico, "produto"), Toast.LENGTH_SHORT).show();
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.id_invalido), Toast.LENGTH_SHORT).show();
            return;
        }
        buscarProdutoParaAtualizar(id);
    }

    private void buscarProdutoParaAtualizar(int produtoId) {
        if (produtoId == -1) return;

        binding.progressBarAtualizarProduto.setVisibility(View.VISIBLE);
        binding.fieldsGroupProduto.setVisibility(View.GONE);
        binding.btnSalvarAtualizacaoProduto.setVisibility(View.GONE);

        executorService.execute(() -> {
            produtoSelecionado = produtoDao.buscarPorId(produtoId);
            runOnUiThread(() -> {
                binding.progressBarAtualizarProduto.setVisibility(View.GONE);
                if (produtoSelecionado != null) {
                    binding.edtNomeProdutoAtualizar.setText(produtoSelecionado.getNome());
                    binding.edtDescricaoProdutoAtualizar.setText(produtoSelecionado.getDescricao());
                    if (produtoSelecionado.getPreco() != null) {
                        binding.edtPrecoProdutoAtualizar.setText(produtoSelecionado.getPreco().setScale(2, RoundingMode.HALF_UP).toPlainString());
                    } else {
                        binding.edtPrecoProdutoAtualizar.setText("0.00");
                    }
                    binding.fieldsGroupProduto.setVisibility(View.VISIBLE);
                    binding.btnSalvarAtualizacaoProduto.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AtualizarProdutoActivity.this, getString(R.string.produto_nao_encontrado), Toast.LENGTH_SHORT).show();
                    binding.fieldsGroupProduto.setVisibility(View.GONE);
                    binding.btnSalvarAtualizacaoProduto.setVisibility(View.GONE);
                }
            });
        });
    }

    private void salvarAtualizacoesDoProduto() {
        if (produtoSelecionado == null) {
            Toast.makeText(this, getString(R.string.buscar_primeiro_generico, "produto"), Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = binding.edtNomeProdutoAtualizar.getText().toString().trim();
        String descricao = binding.edtDescricaoProdutoAtualizar.getText().toString().trim();
        String precoStr = binding.edtPrecoProdutoAtualizar.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(descricao) || TextUtils.isEmpty(precoStr)) {
            Toast.makeText(this, getString(R.string.campos_obrigatorios), Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal precoBigDecimal;
        try {
            String normalizedPrecoStr = precoStr.replace(",", ".");
            precoBigDecimal = new BigDecimal(normalizedPrecoStr);
            if (precoBigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(this, "O preço deve ser um valor positivo.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de preço inválido. Use ponto ou vírgula como separador decimal (ex: 10.50 ou 10,50).", Toast.LENGTH_LONG).show();
            return;
        }

        binding.progressBarAtualizarProduto.setVisibility(View.VISIBLE);
        binding.btnSalvarAtualizacaoProduto.setEnabled(false);
        binding.btnBuscarProdutoAtualizar.setEnabled(false);

        executorService.execute(() -> {
            boolean nomeAlterado = !nome.equals(produtoSelecionado.getNome());
            Produto produtoComNovoNome = null;
            if (nomeAlterado) {
                produtoComNovoNome = produtoDao.buscarPorNome(nome);
            }

            if (nomeAlterado && produtoComNovoNome != null && produtoComNovoNome.getId() != produtoSelecionado.getId()) {
                runOnUiThread(() -> {
                    binding.progressBarAtualizarProduto.setVisibility(View.GONE);
                    binding.btnSalvarAtualizacaoProduto.setEnabled(true);
                    binding.btnBuscarProdutoAtualizar.setEnabled(true);
                    Toast.makeText(AtualizarProdutoActivity.this, getString(R.string.produto_ja_existe), Toast.LENGTH_LONG).show();
                });
                return;
            }

            Produto produtoAtualizado = new Produto();
            produtoAtualizado.setId(produtoSelecionado.getId());
            produtoAtualizado.setNome(nome);
            produtoAtualizado.setDescricao(descricao);
            produtoAtualizado.setPreco(precoBigDecimal);

            int linhasAfetadas = produtoDao.atualizar(produtoAtualizado);

            runOnUiThread(() -> {
                binding.progressBarAtualizarProduto.setVisibility(View.GONE);
                binding.btnSalvarAtualizacaoProduto.setEnabled(true);
                binding.btnBuscarProdutoAtualizar.setEnabled(true);
                if (linhasAfetadas > 0) {
                    Toast.makeText(AtualizarProdutoActivity.this, getString(R.string.produto_atualizado_sucesso), Toast.LENGTH_SHORT).show();
                    VisualizarProdutoActivity.setProdutosDesatualizados(true);
                    if (produtoIdParaCarregar != -1) {
                        finish();
                    }
                } else {
                    Toast.makeText(AtualizarProdutoActivity.this, getString(R.string.falha_atualizar_produto), Toast.LENGTH_LONG).show();
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
