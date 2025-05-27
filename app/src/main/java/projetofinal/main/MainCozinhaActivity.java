package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityMainCozinhaBinding; // Importar a classe de binding

public class MainCozinhaActivity extends AppCompatActivity {

    private ActivityMainCozinhaBinding binding; // Variável para o binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflar o layout usando ViewBinding
        binding = ActivityMainCozinhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar a Toolbar
        Toolbar toolbar = binding.toolbarMainCozinha; // Usando o ID do binding
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Painel da Cozinha");
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Se precisar de botão voltar
        }

        binding.btnReceberComandas.setOnClickListener(v -> {
            startActivity(new Intent(MainCozinhaActivity.this, ReceberComandasActivity.class));
        });

        // Configurar o botão de logout
        binding.btnLogoutCozinha.setOnClickListener(v -> {
            logoutCozinha();
        });
    }

    private void logoutCozinha() {
        // Limpar SharedPreferences (as mesmas chaves usadas no LoginActivity)
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LoginActivity.KEY_USER_ID);
        editor.remove(LoginActivity.KEY_USER_ROLE);
        editor.remove(LoginActivity.KEY_USER_NOME);
        editor.apply();

        // Redirecionar para LoginActivity
        Intent intent = new Intent(MainCozinhaActivity.this, LoginActivity.class);
        // Limpa a pilha de activities para que o usuário não possa voltar para esta tela
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Fecha a MainCozinhaActivity
    }
}
