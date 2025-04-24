package projetofinal.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityVisualizarEstoqueBinding;

public class VisualizarEstoqueActivity extends AppCompatActivity {

    private ActivityVisualizarEstoqueBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarEstoqueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Aqui você vai pegar os dados do estoque do banco
        String[] estoqueArray = {
                "Produto: Hambúrguer - Estoque: 20",
                "Produto: Batata Frita - Estoque: 30",
                "Produto: Refrigerante - Estoque: 25"
        };

        // Exibir os itens de estoque
        binding.txtEstoque.setText(String.join("\n", estoqueArray));
    }
}
