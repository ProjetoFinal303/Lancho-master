package projetofinal.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.databinding.ActivityVisualizarProdutoBinding;
import java.util.ArrayList;
import java.util.List;
import projetofinal.adapters.ProdutoAdapter;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Produto;

public class VisualizarProdutoActivity extends AppCompatActivity {

    private ActivityVisualizarProdutoBinding binding;
    private ProdutoDao produtoDao;
    private ProdutoAdapter produtoAdapter;
    private List<Produto> listaDeProdutos = new ArrayList<>();
    private static boolean produtosDesatualizados = true;
    private static final String TAG = "VisualizarProduto";

    public static void setProdutosDesatualizados(boolean status) {
        produtosDesatualizados = status;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        produtoDao = new ProdutoDao(this);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        binding.recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(this));
        produtoAdapter = new ProdutoAdapter(this, listaDeProdutos, (produto, view) -> {
            // Lógica de clique, como adicionar ao carrinho
        });
        binding.recyclerViewProdutos.setAdapter(produtoAdapter);
    }

    private void carregarProdutos() {
        setLoading(true);
        produtoDao.listarTodos(
                produtos -> runOnUiThread(() -> {
                    setLoading(false);
                    if (produtos != null && !produtos.isEmpty()) {
                        listaDeProdutos.clear();
                        listaDeProdutos.addAll(produtos);
                        produtoAdapter.atualizarProdutos(listaDeProdutos);
                        binding.recyclerViewProdutos.setVisibility(View.VISIBLE);
                        binding.textViewNenhumProduto.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewProdutos.setVisibility(View.GONE);
                        binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                    }
                    setProdutosDesatualizados(false);
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao carregar produtos: ", error);
                    Toast.makeText(this, "Erro ao carregar produtos.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (produtosDesatualizados) {
            carregarProdutos();
        }
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarProdutos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}