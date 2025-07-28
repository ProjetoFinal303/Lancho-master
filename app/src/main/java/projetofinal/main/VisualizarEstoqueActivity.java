package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityVisualizarEstoqueBinding;
import java.util.ArrayList;
import java.util.List;
import projetofinal.adapters.EstoqueAdapter;
import projetofinal.dao.EstoqueDao;
import projetofinal.models.Estoque;

public class VisualizarEstoqueActivity extends BaseActivity {

    private ActivityVisualizarEstoqueBinding binding;
    private EstoqueDao estoqueDao;
    private EstoqueAdapter estoqueAdapter;
    private List<Estoque> listaDeEstoque = new ArrayList<>();
    private static boolean estoqueDesatualizado = true;
    private static final String TAG = "VisualizarEstoque";

    public static void setEstoqueDesatualizado(boolean status) {
        estoqueDesatualizado = status;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarEstoqueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarVisualizarEstoque;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.visualizar_estoque_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        estoqueDao = new EstoqueDao(this);
        setupRecyclerView();

        // Removemos o botão que levava para a tela de cadastrar produto local
        binding.btnIrCadastrarProduto.setVisibility(View.GONE);
        // O botão de atualizar estoque também não é mais necessário, pois a lógica estará no clique do item
        binding.btnIrAtualizarEstoque.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        binding.recyclerViewEstoque.setLayoutManager(new LinearLayoutManager(this));
        // O clique em um item do estoque agora abre um diálogo para edição
        estoqueAdapter = new EstoqueAdapter(this, listaDeEstoque, this::mostrarDialogoAtualizarEstoque);
        binding.recyclerViewEstoque.setAdapter(estoqueAdapter);
    }

    private void mostrarDialogoAtualizarEstoque(Estoque estoqueItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atualizar Estoque");
        builder.setMessage("Produto: " + estoqueItem.getNomeProduto());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nova quantidade");
        input.setText(String.valueOf(estoqueItem.getQuantidade()));
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novaQuantidadeStr = input.getText().toString();
            if (!novaQuantidadeStr.isEmpty()) {
                try {
                    int novaQuantidade = Integer.parseInt(novaQuantidadeStr);
                    estoqueItem.setQuantidade(novaQuantidade);
                    salvarAtualizacaoEstoque(estoqueItem);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Quantidade inválida.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void salvarAtualizacaoEstoque(Estoque estoqueParaSalvar) {
        setLoading(true);
        estoqueDao.inserirOuAtualizar(estoqueParaSalvar,
                response -> runOnUiThread(() -> {
                    Toast.makeText(this, "Estoque atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    // Recarrega a lista para mostrar o novo valor
                    carregarEstoque();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao salvar estoque: ", error);
                    Toast.makeText(this, "Erro ao salvar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void carregarEstoque() {
        setLoading(true);
        estoqueDao.listarTodosComNomeProduto(
                estoqueCarregado -> runOnUiThread(() -> {
                    setLoading(false);
                    if (estoqueCarregado != null && !estoqueCarregado.isEmpty()) {
                        listaDeEstoque.clear();
                        listaDeEstoque.addAll(estoqueCarregado);
                        estoqueAdapter.updateEstoqueList(listaDeEstoque);
                        binding.recyclerViewEstoque.setVisibility(View.VISIBLE);
                        binding.textViewNenhumEstoque.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewEstoque.setVisibility(View.GONE);
                        binding.textViewNenhumEstoque.setVisibility(View.VISIBLE);
                    }
                    setEstoqueDesatualizado(false);
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao carregar estoque: ", error);
                    binding.textViewNenhumEstoque.setText("Erro ao carregar estoque.");
                    binding.textViewNenhumEstoque.setVisibility(View.VISIBLE);
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarEstoque.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (estoqueDesatualizado) {
            carregarEstoque();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}