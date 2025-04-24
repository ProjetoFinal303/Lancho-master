package projetofinal.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityTelaAdmBinding;

public class TelaAdmActivity extends AppCompatActivity {
    private ActivityTelaAdmBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTelaAdmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textAdm.setText("Bem-vindo, Administrador!");
    }
}
