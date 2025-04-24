package projetofinal.main;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.R;

public class VerProdutosActivity extends AppCompatActivity {
    private TextView txtProdutos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_produtos);

        txtProdutos = findViewById(R.id.txtProdutos);

        // Carregar produtos do XML
        String[] produtosArray = getResources().getStringArray(R.array.produtos_lista);
        txtProdutos.setText(String.join("\n", produtosArray));
    }
}
