package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityProfileBinding;
import org.json.JSONObject;
import projetofinal.dao.ClienteDao;
import projetofinal.database.SupabaseStorageClient;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private ClienteDao clienteDao;
    private int clienteIdLogado = -1;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarProfile;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadAvatar(imageUri);
                    }
                });

        clienteDao = new ClienteDao(this);
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        binding.btnEditarDados.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AtualizarClienteActivity.class);
            intent.putExtra("CLIENTE_ID_EDITAR", clienteIdLogado);
            startActivity(intent);
        });

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void uploadAvatar(Uri imageUri) {
        Toast.makeText(this, "Enviando nova foto...", Toast.LENGTH_SHORT).show();
        SupabaseStorageClient.uploadFile(this, imageUri,
                publicUrl -> runOnUiThread(() -> {
                    try {
                        JSONObject payload = new JSONObject();
                        payload.put("avatar_url", publicUrl);
                        clienteDao.atualizarParcial(clienteIdLogado, payload,
                                response -> runOnUiThread(() -> {
                                    Toast.makeText(this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();
                                    carregarDadosDoCliente(); // Recarrega para mostrar a nova foto
                                }),
                                error -> runOnUiThread(() -> Toast.makeText(this, "Erro ao salvar URL da foto.", Toast.LENGTH_SHORT).show())
                        );
                    } catch (Exception e) {
                        //...
                    }
                }),
                error -> runOnUiThread(() -> Toast.makeText(this, "Falha no upload da imagem.", Toast.LENGTH_SHORT).show())
        );
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDadosDoCliente();
    }

    private void carregarDadosDoCliente() {
        if (clienteIdLogado != -1) {
            clienteDao.buscarPorId(clienteIdLogado, cliente -> {
                if (cliente != null) {
                    runOnUiThread(() -> {
                        binding.textViewProfileNome.setText(cliente.getNome());
                        binding.textViewProfileEmail.setText(cliente.getEmail());

                        // Carrega a foto de perfil com Glide
                        Glide.with(this)
                                .load(cliente.getAvatarUrl()) // Usa o novo campo getAvatarUrl()
                                .placeholder(R.drawable.ic_person) // Imagem padrão
                                .error(R.drawable.ic_person) // Imagem de erro
                                .into(binding.profileImage);
                    });
                }
            }, error -> runOnUiThread(() ->
                    Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show()
            ));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}