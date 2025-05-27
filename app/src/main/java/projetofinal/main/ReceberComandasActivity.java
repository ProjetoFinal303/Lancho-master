package projetofinal.main;

import android.os.Bundle;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import projetofinal.adapters.PedidoAdapterCozinha;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class ReceberComandasActivity extends AppCompatActivity implements PedidoAdapterCozinha.OnComandaInteractionListener {

    private ActivityReceberComandasBinding binding;
    private PedidoDao pedidoDao;
    private PedidoAdapterCozinha comandasAdapter;
    private List<Pedido> listaDeComandas;
    private ExecutorService executorService;

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
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        pedidoDao = new PedidoDao(this);
        executorService = Executors.newSingleThreadExecutor();
        listaDeComandas = new ArrayList<>();

        setupRecyclerView();
        // carregarComandasAtivas() será chamado no onResume
    }

    private void setupRecyclerView() {
        binding.recyclerViewComandasCozinha.setLayoutManager(new LinearLayoutManager(this));
        comandasAdapter = new PedidoAdapterCozinha(this, listaDeComandas, this);
        binding.recyclerViewComandasCozinha.setAdapter(comandasAdapter);
    }

    private void carregarComandasAtivas() {
        binding.progressBarComandas.setVisibility(View.VISIBLE);
        binding.textViewNenhumaComanda.setVisibility(View.GONE);
        binding.recyclerViewComandasCozinha.setVisibility(View.GONE);

        executorService.execute(() -> {
            List<Pedido> todasComandas = pedidoDao.listarTodos();
            // Filtrar para mostrar apenas "Pendente" ou "Em Preparo"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                listaDeComandas = todasComandas.stream()
                        .filter(p -> "Pendente".equalsIgnoreCase(p.getStatus()) || "Em Preparo".equalsIgnoreCase(p.getStatus()))
                        .collect(Collectors.toList());
            } else { // Fallback para versões mais antigas
                listaDeComandas.clear();
                for(Pedido p : todasComandas){
                    if("Pendente".equalsIgnoreCase(p.getStatus()) || "Em Preparo".equalsIgnoreCase(p.getStatus())){
                        listaDeComandas.add(p);
                    }
                }
            }


            runOnUiThread(() -> {
                binding.progressBarComandas.setVisibility(View.GONE);
                if (listaDeComandas != null && !listaDeComandas.isEmpty()) {
                    comandasAdapter.atualizarComandas(listaDeComandas);
                    binding.recyclerViewComandasCozinha.setVisibility(View.VISIBLE);
                } else {
                    binding.textViewNenhumaComanda.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    public void onStatusChangeClicked(Pedido pedido, String novoStatus) {
        binding.progressBarComandas.setVisibility(View.VISIBLE); // Mostrar progresso
        executorService.execute(() -> {
            int linhasAfetadas = pedidoDao.atualizarStatus(pedido.getId(), novoStatus);
            runOnUiThread(() -> {
                binding.progressBarComandas.setVisibility(View.GONE); // Esconder progresso
                if (linhasAfetadas > 0) {
                    Toast.makeText(this, "Status do pedido #" + pedido.getId() + " atualizado para " + novoStatus, Toast.LENGTH_SHORT).show();
                    carregarComandasAtivas(); // Recarrega a lista para refletir a mudança
                } else {
                    Toast.makeText(this, "Falha ao atualizar status do pedido.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarComandasAtivas();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
