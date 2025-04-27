package projetofinal.main;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projetofinal.databinding.ActivityVisualizarProdutoBinding;

import java.util.List;

import projetofinal.adapters.ProdutoAdapter;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Produto;

public class VisualizarProdutoActivity extends AppCompatActivity {

    private ActivityVisualizarProdutoBinding binding;  // Instância do ViewBinding
    private ProdutoAdapter produtoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializando o ViewBinding e definindo o layout
        binding = ActivityVisualizarProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  // Usando o root da ViewBinding

        // Usando o ViewBinding para acessar o RecyclerView diretamente
        binding.recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(this));

        ProdutoDao produtoDao = new ProdutoDao(this);
        List<Produto> produtos = produtoDao.listarTodos();

        if (produtos.isEmpty()) {
            Toast.makeText(this, "Não há produtos cadastrados", Toast.LENGTH_SHORT).show();
        } else {
            produtoAdapter = new ProdutoAdapter(produtos);
            binding.recyclerViewProdutos.setAdapter(produtoAdapter);  // Usando o ViewBinding para definir o Adapter
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;  // Liberando o binding ao destruir a Activity
    }
}
