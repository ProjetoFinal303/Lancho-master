package projetofinal.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projetofinal.databinding.ActivityTelaCozinhaBinding;

public class TelaCozinhaActivity extends AppCompatActivity {
    private ActivityTelaCozinhaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTelaCozinhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textCozinha.setText("Área da Cozinha");
    }
}
