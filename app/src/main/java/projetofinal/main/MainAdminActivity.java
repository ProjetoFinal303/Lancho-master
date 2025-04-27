package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.R;

public class MainAdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        findViewById(R.id.btnProdutos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainAdminActivity.this, VisualizarProdutoActivity.class));
            }
        });

        findViewById(R.id.btnEstoque).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainAdminActivity.this, VisualizarEstoqueActivity.class));
            }
        });

        findViewById(R.id.btnPedidos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainAdminActivity.this, VisualizarPedidosActivity.class));
            }
        });

        findViewById(R.id.btnClientes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainAdminActivity.this, VisualizarClienteActivity.class));
            }
        });

        findViewById(R.id.btnMeusPedidosCliente).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainAdminActivity.this, VisualizarPedidosActivity.class));
            }
        });
    }
}
