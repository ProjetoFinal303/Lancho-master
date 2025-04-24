package projetofinal.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityAtualizarEstoqueBinding;

public class AtualizarEstoqueActivity extends AppCompatActivity {

    private ActivityAtualizarEstoqueBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtualizarEstoqueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lógica para atualizar estoque, exemplo:
        binding.btnAtualizarEstoque.setOnClickListener(v -> {
            String produtoID = binding.edtProdutoID.getText().toString();
            String quantidade = binding.edtQuantidade.getText().toString();
            // Atualiza o estoque do produto
        });
    }
}
