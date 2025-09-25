package projetofinal.main;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityReceberComandasBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import projetofinal.adapters.PedidoAdapterCozinha;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

// A interface que causava o erro foi removida daqui
public class ReceberComandasActivity extends BaseActivity {

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
        // A lógica de clique agora é passada como uma função lambda, como na MainCozinhaActivity
        comandasAdapter = new PedidoAdapterCozinha(listaDeComandas, this, this::onStatusChangeClicked);
        binding.recyclerViewComandasCozinha.setAdapter(comandasAdapter);
    }

    private void carregarComandasAtivas() {
        setLoading(true);
        List<String> statusParaBuscar = Arrays.asList("pendente", "em preparo");
        pedidoDao.getPedidosPorStatus(statusParaBuscar,
                pedidos -> runOnUiThread(() -> {
                    setLoading(false);
                    listaDeComandas.clear();
                    if (pedidos != null) {
                        listaDeComandas.addAll(pedidos);
                    }

                    comandasAdapter.notifyDataSetChanged();

                    if (!listaDeComandas.isEmpty()) {
                        binding.recyclerViewComandasCozinha.setVisibility(View.VISIBLE);
                        binding.textViewNenhumaComanda.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewComandasCozinha.setVisibility(View.GONE);
                        binding.textViewNenhumaComanda.setVisibility(View.VISIBLE);
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao carregar comandas: " + error);
                    Toast.makeText(this, "Erro ao carregar comandas.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    // Este método agora é chamado pela função lambda no adapter
    public void onStatusChangeClicked(Pedido pedido) {
        String novoStatus;
        if ("pendente".equalsIgnoreCase(pedido.getStatus())) {
            novoStatus = "em preparo";
        } else if ("em preparo".equalsIgnoreCase(pedido.getStatus())) {
            novoStatus = "concluido";
        } else {
            return; // Não faz nada se o status for outro
        }

        setLoading(true);
        pedidoDao.updateStatus(pedido.getId(), novoStatus,
                () -> runOnUiThread(() -> {
                    Toast.makeText(this, "Status do pedido #" + pedido.getId() + " atualizado!", Toast.LENGTH_SHORT).show();
                    carregarComandasAtivas(); // Recarrega a lista para refletir a mudança
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao atualizar status: " + error);
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