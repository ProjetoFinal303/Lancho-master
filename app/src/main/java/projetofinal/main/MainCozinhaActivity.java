package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityMainCozinhaBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import projetofinal.adapters.PedidoAdapterCozinha;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class MainCozinhaActivity extends BaseActivity {

    private ActivityMainCozinhaBinding binding;
    private PedidoAdapterCozinha pedidoAdapter;
    private List<Pedido> pedidoList = new ArrayList<>();
    private PedidoDao pedidoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainCozinhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pedidoDao = new PedidoDao(this);
        setupRecyclerView();
        setupListeners();
        carregarPedidos();
    }

    private void setupRecyclerView() {
        binding.recyclerViewPedidosCozinha.setLayoutManager(new LinearLayoutManager(this));
        pedidoAdapter = new PedidoAdapterCozinha(pedidoList, this, this::mudarStatusPedido);
        binding.recyclerViewPedidosCozinha.setAdapter(pedidoAdapter);
    }

    private void setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::carregarPedidos);
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                fazerLogout();
                return true;
            }
            return false;
        });
    }

    private void carregarPedidos() {
        binding.swipeRefreshLayout.setRefreshing(true);
        List<String> statusParaBuscar = Arrays.asList("pendente", "em preparo");

        pedidoDao.getPedidosPorStatus(statusParaBuscar,
                pedidos -> runOnUiThread(() -> {
                    pedidoList.clear();
                    if (pedidos != null) {
                        pedidoList.addAll(pedidos);
                    }
                    pedidoAdapter.notifyDataSetChanged();
                    binding.swipeRefreshLayout.setRefreshing(false);
                    verificarListaVazia();
                }),
                error -> runOnUiThread(() -> {
                    Toast.makeText(this, "Erro ao carregar pedidos: " + error, Toast.LENGTH_SHORT).show();
                    binding.swipeRefreshLayout.setRefreshing(false);
                    verificarListaVazia();
                })
        );
    }

    private void mudarStatusPedido(Pedido pedido) {
        String statusAtual = pedido.getStatus();
        String novoStatus;

        if ("pendente".equalsIgnoreCase(statusAtual)) {
            novoStatus = "em preparo";
        } else if ("em preparo".equalsIgnoreCase(statusAtual)) {
            novoStatus = "saiu para entrega"; // <-- ALTERAÇÃO AQUI
        } else {
            return;
        }

        pedidoDao.updateStatus(pedido.getId(), novoStatus,
                () -> runOnUiThread(() -> {
                    Toast.makeText(this, "Status do pedido #" + pedido.getId() + " atualizado!", Toast.LENGTH_SHORT).show();
                    carregarPedidos();
                }),
                error -> runOnUiThread(() -> {
                    Toast.makeText(this, "Erro ao atualizar status: " + error, Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void verificarListaVazia() {
        if (pedidoList.isEmpty()) {
            binding.recyclerViewPedidosCozinha.setVisibility(View.GONE);
            binding.textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewPedidosCozinha.setVisibility(View.VISIBLE);
            binding.textViewEmpty.setVisibility(View.GONE);
        }
    }

    private void fazerLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        Intent intent = new Intent(MainCozinhaActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}