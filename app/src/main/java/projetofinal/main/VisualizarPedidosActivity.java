package projetofinal.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityVisualizarPedidosBinding;

public class VisualizarPedidosActivity extends AppCompatActivity {

    private ActivityVisualizarPedidosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Aqui você vai pegar os dados de pedido do banco
        String[] pedidosArray = {
                "Pedido 1: Hambúrguer + Batata Frita",
                "Pedido 2: Coxinha + Refrigerante",
                "Pedido 3: Pastel + Guaracamp"
        };

        // Exibir os pedidos na tela
        binding.txtPedidos.setText(String.join("\n", pedidosArray));
    }
}
