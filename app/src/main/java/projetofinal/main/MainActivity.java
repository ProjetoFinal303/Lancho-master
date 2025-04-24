package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "Tela principal carregada!");

        // Configuração dos botões com binding
        binding.btnCadastrar.setOnClickListener(v -> {
            Log.d(TAG, "Botão Cadastrar clicado!");
            startActivity(new Intent(this, CadastrarClienteActivity.class));  // Navega para cadastro de cliente
        });

        binding.btnVisualizar.setOnClickListener(v -> {
            Log.d(TAG, "Botão Visualizar clicado!");
            startActivity(new Intent(this, VisualizarClienteActivity.class));  // Navega para visualizar clientes
        });

        binding.btnAtualizar.setOnClickListener(v -> {
            Log.d(TAG, "Botão Atualizar clicado!");
            startActivity(new Intent(this, AtualizarClienteActivity.class));  // Navega para atualizar clientes
        });

        binding.btnExcluir.setOnClickListener(v -> {
            Log.d(TAG, "Botão Excluir clicado!");
            startActivity(new Intent(this, ExcluirClienteActivity.class));  // Navega para excluir clientes
        });

        binding.btnVerProdutos.setOnClickListener(v -> {
            Log.d(TAG, "Botão Ver Produtos clicado!");
            startActivity(new Intent(this, VisualizarProdutoActivity.class));  // Navega para visualizar produtos
        });
    }
}
