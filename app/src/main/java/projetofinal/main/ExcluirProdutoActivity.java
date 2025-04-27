package projetofinal.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.databinding.ActivityExcluirProdutoBinding;

public class ExcluirProdutoActivity extends AppCompatActivity {

    private ActivityExcluirProdutoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Aqui você faria o processo de exclusão, talvez chamando um método no banco
        binding.btnExcluirProduto.setOnClickListener(v -> {
            String produtoID = binding.edtProdutoID.getText().toString();
            // Lógica para excluir produto com base no ID
        });
    }
}
