package projetofinal.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.databinding.ActivityCadastrarPedidoBinding;

public class CadastrarPedidoActivity extends AppCompatActivity {

    private ActivityCadastrarPedidoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastrarPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Aqui você fará o cadastro do pedido com dados do banco
        binding.btnCadastrarPedido.setOnClickListener(v -> {
            // Lógica de cadastro de pedido
        });
    }
}
