package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityVisualizarEstoqueBinding;
import java.util.ArrayList;
import java.util.List;
import projetofinal.adapters.EstoqueAdapter;
import projetofinal.dao.EstoqueDao;
import projetofinal.models.Estoque;

public class VisualizarEstoqueActivity extends AppCompatActivity {

    private ActivityVisualizarEstoqueBinding binding;
    private EstoqueDao estoqueDao;
    private EstoqueAdapter estoqueAdapter;
    private List<Estoque> listaDeEstoque = new ArrayList<>();
    private static boolean estoqueDesatualizado = true;
    private static final String TAG = "VisualizarEstoque";

    public static void setEstoqueDesatualizado(boolean status) {
        Log.d(TAG, "setEstoqueDesatualizado chamado com status: " + status);
        estoqueDesatualizado = status;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarEstoqueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarVisualizarEstoque;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.visualizar_estoque_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        estoqueDao = new EstoqueDao(this);
        setupRecyclerView();

        binding.btnIrCadastrarProduto.setOnClickListener(v -> {
            VisualizarEstoqueActivity.setEstoqueDesatualizado(true);
            VisualizarProdutoActivity.setProdutosDesatualizados(true);
            startActivity(new Intent(this, CadastrarProdutoActivity.class));
        });

        binding.btnIrAtualizarEstoque.setOnClickListener(v -> {
            startActivity(new Intent(this, AtualizarEstoqueActivity.class));
        });
    }

    private void setupRecyclerView() {
        binding.recyclerViewEstoque.setLayoutManager(new LinearLayoutManager(this));
        estoqueAdapter = new EstoqueAdapter(this, listaDeEstoque, estoqueItem -> {
            Intent intent = new Intent(VisualizarEstoqueActivity.this, AtualizarEstoqueActivity.class);
            intent.putExtra("PRODUTO_ID_ESTOQUE", estoqueItem.getProdutoId());
            startActivity(intent);
        });
        binding.recyclerViewEstoque.setAdapter(estoqueAdapter);
    }

    private void carregarEstoque() {
        setLoading(true);
        estoqueDao.listarTodosComNomeProduto(
                // Callback de Sucesso
                estoqueCarregado -> runOnUiThread(() -> {
                    setLoading(false);
                    if (estoqueCarregado != null && !estoqueCarregado.isEmpty()) {
                        listaDeEstoque.clear();
                        listaDeEstoque.addAll(estoqueCarregado);
                        estoqueAdapter.updateEstoqueList(listaDeEstoque);
                        binding.recyclerViewEstoque.setVisibility(View.VISIBLE);
                        binding.textViewNenhumEstoque.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewEstoque.setVisibility(View.GONE);
                        binding.textViewNenhumEstoque.setVisibility(View.VISIBLE);
                    }
                    setEstoqueDesatualizado(false);
                }),
                // Callback de Erro
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao carregar estoque: ", error);
                    binding.textViewNenhumEstoque.setText("Erro ao carregar estoque.");
                    binding.textViewNenhumEstoque.setVisibility(View.VISIBLE);
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarEstoque.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (estoqueDesatualizado) {
            carregarEstoque();
        }
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