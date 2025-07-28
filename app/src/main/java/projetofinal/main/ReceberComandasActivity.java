package projetofinal.main;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityReceberComandasBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import projetofinal.adapters.PedidoAdapterCozinha;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class ReceberComandasActivity extends BaseActivity implements PedidoAdapterCozinha.OnComandaInteractionListener {

    private ActivityReceberComandasBinding binding;
    private PedidoDao pedidoDao;
    private PedidoAdapterCozinha comandasAdapter;
    private List<Pedido> listaDeComandas = new ArrayList<>();
    private static final String TAG = "ReceberComandas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceberComandasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarReceberComandas;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Comandas Ativas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pedidoDao = new PedidoDao(this);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        binding.recyclerViewComandasCozinha.setLayoutManager(new LinearLayoutManager(this));
        comandasAdapter = new PedidoAdapterCozinha(this, listaDeComandas, this);
        binding.recyclerViewComandasCozinha.setAdapter(comandasAdapter);
    }

    private void carregarComandasAtivas() {
        setLoading(true);
        pedidoDao.listarTodos(
                todasComandas -> runOnUiThread(() -> {
                    setLoading(false);
                    if (todasComandas != null) {
                        listaDeComandas = todasComandas.stream()
                                .filter(p -> "Pendente".equalsIgnoreCase(p.getStatus()) || "Em Preparo".equalsIgnoreCase(p.getStatus()))
                                .collect(Collectors.toList());

                        if (!listaDeComandas.isEmpty()) {
                            comandasAdapter.atualizarComandas(listaDeComandas);
                            binding.recyclerViewComandasCozinha.setVisibility(View.VISIBLE);
                            binding.textViewNenhumaComanda.setVisibility(View.GONE);
                        } else {
                            binding.recyclerViewComandasCozinha.setVisibility(View.GONE);
                            binding.textViewNenhumaComanda.setVisibility(View.VISIBLE);
                        }
                    } else {
                        binding.textViewNenhumaComanda.setVisibility(View.VISIBLE);
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao carregar comandas: ", error);
                    Toast.makeText(this, "Erro ao carregar comandas.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    @Override
    public void onStatusChangeClicked(Pedido pedido, String novoStatus) {
        setLoading(true);
        pedidoDao.atualizarStatus(pedido.getId(), novoStatus,
                response -> runOnUiThread(() -> {
                    Toast.makeText(this, "Status do pedido #" + pedido.getId() + " atualizado!", Toast.LENGTH_SHORT).show();
                    carregarComandasAtivas(); // Recarrega a lista para refletir a mudança
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao atualizar status: ", error);
                    Toast.makeText(this, "Falha ao atualizar status.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarComandasAtivas();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarComandas.setVisibility(isLoading ? View.VISIBLE : View.GONE);
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