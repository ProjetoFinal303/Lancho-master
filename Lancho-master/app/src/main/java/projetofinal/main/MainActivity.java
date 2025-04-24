package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.R;

public class MainActivity extends AppCompatActivity {

    
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Tela principal carregada!");

        // Ligação dos botões do layout aos métodos de clique
        Button btnCadastrar = findViewById(R.id.btnCadastrar);
        Button btnVisualizar = findViewById(R.id.btnVisualizar);
        Button btnAtualizar = findViewById(R.id.btnAtualizar);
        Button btnExcluir = findViewById(R.id.btnExcluir);
        Button btnVerProdutos = findViewById(R.id.btnVerProdutos);

        // Configuração dos botões
        btnCadastrar.setOnClickListener(v -> {
            Log.d(TAG, "Botão Cadastrar clicado!");
            startActivity(new Intent(this, CadastrarClienteActivity.class));
        });

        btnVisualizar.setOnClickListener(v -> {
            Log.d(TAG, "Botão Visualizar clicado!");
            // Ao clicar, vai para a tela de visualização de clientes
            startActivity(new Intent(this, VisualizarClienteActivity.class));
        });

        btnAtualizar.setOnClickListener(v -> {
            Log.d(TAG, "Botão Atualizar clicado!");
            startActivity(new Intent(this, AtualizarClienteActivity.class));
        });

        btnExcluir.setOnClickListener(v -> {
            Log.d(TAG, "Botão Excluir clicado!");
            startActivity(new Intent(this, ExcluirClienteActivity.class));
        });

        btnVerProdutos.setOnClickListener(v -> {
            Log.d(TAG, "Botão Ver Produtos clicado!");
            startActivity(new Intent(this, VerProdutosActivity.class));
        });
    }
}
