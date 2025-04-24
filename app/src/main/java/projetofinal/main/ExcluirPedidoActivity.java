package projetofinal.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityExcluirPedidoBinding;

public class ExcluirPedidoActivity extends AppCompatActivity {

    private ActivityExcluirPedidoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExcluirPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Aqui você faria o processo de exclusão do pedido
        binding.btnExcluirPedido.setOnClickListener(v -> {
            String pedidoID = binding.edtPedidoID.getText().toString();
            // Lógica para excluir pedido com base no ID
        });
    }
}
