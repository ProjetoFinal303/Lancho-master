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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.adapters.EstoqueAdapter;
import projetofinal.dao.EstoqueDao;
import projetofinal.models.Estoque;

public class VisualizarEstoqueActivity extends AppCompatActivity {

    private ActivityVisualizarEstoqueBinding binding;
    private EstoqueDao estoqueDao;
    private EstoqueAdapter estoqueAdapter;
    private ExecutorService executorService;
    private List<Estoque> listaDeEstoque;

    private static boolean estoqueDesatualizado = true; // Inicia como true para forçar a primeira carga
    private static final String TAG = "VisualizarEstoque";

    /**
     * Método estático para ser chamado por outras activities quando o estoque é modificado.
     * @param status true se o estoque foi modificado e precisa ser recarregado, false caso contrário.
     */
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
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        estoqueDao = new EstoqueDao(this);
        executorService = Executors.newSingleThreadExecutor();
        listaDeEstoque = new ArrayList<>();

        setupRecyclerView();

        binding.btnIrCadastrarProduto.setOnClickListener(v -> {
            VisualizarEstoqueActivity.setEstoqueDesatualizado(true);
            VisualizarProdutoActivity.setProdutosDesatualizados(true);
            startActivity(new Intent(this, CadastrarProdutoActivity.class));
        });

        binding.btnIrAtualizarEstoque.setOnClickListener(v -> {
            // A AtualizarEstoqueActivity deve chamar setEstoqueDesatualizado(true) ao salvar.
            startActivity(new Intent(this, AtualizarEstoqueActivity.class));
        });
        Log.d(TAG, "onCreate finalizado.");
    }

    private void setupRecyclerView() {
        binding.recyclerViewEstoque.setLayoutManager(new LinearLayoutManager(this));
        estoqueAdapter = new EstoqueAdapter(this, listaDeEstoque, estoqueItem -> {
            Intent intent = new Intent(VisualizarEstoqueActivity.this, AtualizarEstoqueActivity.class);
            intent.putExtra("PRODUTO_ID_ESTOQUE", estoqueItem.getProdutoId());
            startActivity(intent);
        });
        binding.recyclerViewEstoque.setAdapter(estoqueAdapter);
        Log.d(TAG, "RecyclerView configurado.");
    }

    private void carregarEstoque() {
        Log.i(TAG, "Método carregarEstoque() CHAMADO. Flag desatualizado: " + estoqueDesatualizado);
        binding.progressBarEstoque.setVisibility(View.VISIBLE);
        binding.textViewNenhumEstoque.setVisibility(View.GONE);
        binding.recyclerViewEstoque.setVisibility(View.GONE);

        executorService.execute(() -> {
            List<Estoque> estoqueCarregadoDoBanco = estoqueDao.listarTodosComNomeProduto();
            final List<Estoque> finalEstoqueCarregado = new ArrayList<>(estoqueCarregadoDoBanco != null ? estoqueCarregadoDoBanco : new ArrayList<>());
            Log.d(TAG, "Estoque carregado do DAO na background thread: " + finalEstoqueCarregado.size());

            runOnUiThread(() -> {
                Log.d(TAG, "Atualizando UI em carregarEstoque(). Estoque carregado: " + finalEstoqueCarregado.size());
                listaDeEstoque.clear();
                listaDeEstoque.addAll(finalEstoqueCarregado);

                binding.progressBarEstoque.setVisibility(View.GONE);
                if (!listaDeEstoque.isEmpty()) {
                    estoqueAdapter.updateEstoqueList(listaDeEstoque); // Passa a referência da lista de instância
                    binding.recyclerViewEstoque.setVisibility(View.VISIBLE);
                    Log.d(TAG, "RecyclerView atualizado e visível com " + estoqueAdapter.getItemCount() + " itens de estoque.");
                } else {
                    binding.textViewNenhumEstoque.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Nenhum item de estoque para exibir. textViewNenhumEstoque visível.");
                }
                estoqueDesatualizado = false; // Reseta o flag após carregar
                Log.d(TAG, "Flag estoqueDesatualizado resetado para false.");
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume chamado. Flag estoqueDesatualizado: " + estoqueDesatualizado);
        if (estoqueDesatualizado || (estoqueAdapter != null && estoqueAdapter.getItemCount() == 0 && listaDeEstoque.isEmpty())) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
