package projetofinal.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.projetofinal.databinding.FragmentPedidosBinding;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import projetofinal.adapters.PedidoAdapterCliente;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class PedidosFragment extends Fragment {

    private FragmentPedidosBinding binding;
    private PedidoDao pedidoDao;
    private PedidoAdapterCliente pedidoAdapter;
    private List<Pedido> pedidoList = new ArrayList<>();
    private static final String TAG = "PedidosFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPedidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pedidoDao = new PedidoDao(getContext());
        setupRecyclerView();
        carregarPedidos();
    }

    private void setupRecyclerView() {
        binding.recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(getContext()));
        // Passa o método de confirmação para o adapter
        pedidoAdapter = new PedidoAdapterCliente(pedidoList, getContext(), this::confirmarEntrega);
        binding.recyclerViewPedidos.setAdapter(pedidoAdapter);
    }

    private void carregarPedidos() {
        binding.progressBarPedidos.setVisibility(View.VISIBLE);
        binding.textViewNenhumPedido.setVisibility(View.GONE);
        binding.recyclerViewPedidos.setVisibility(View.GONE);

        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (clienteId == -1) {
            Toast.makeText(getContext(), "Erro: ID do cliente não encontrado.", Toast.LENGTH_SHORT).show();
            binding.progressBarPedidos.setVisibility(View.GONE);
            binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
            return;
        }

        pedidoDao.buscarPedidosPorClienteId(clienteId,
                pedidos -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            pedidoList.clear();
                            if (pedidos != null && !pedidos.isEmpty()) {
                                pedidos.sort(Comparator.comparingInt(Pedido::getId).reversed());
                                pedidoList.addAll(pedidos);
                            }
                            pedidoAdapter.notifyDataSetChanged();
                            verificarListaVazia();
                            binding.progressBarPedidos.setVisibility(View.GONE);
                        });
                    }
                },
                error -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Erro ao carregar pedidos: ", error);
                            Toast.makeText(getContext(), "Falha ao carregar pedidos.", Toast.LENGTH_SHORT).show();
                            verificarListaVazia();
                            binding.progressBarPedidos.setVisibility(View.GONE);
                        });
                    }
                }
        );
    }

    private void confirmarEntrega(Pedido pedido) {
        binding.progressBarPedidos.setVisibility(View.VISIBLE);
        pedidoDao.updateStatus(pedido.getId(), "concluido",
                () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Pedido #" + pedido.getId() + " concluído! Bom apetite!", Toast.LENGTH_LONG).show();
                            carregarPedidos(); // Recarrega a lista para mostrar a mudança
                        });
                    }
                },
                error -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Erro ao confirmar entrega: " + error);
                            Toast.makeText(getContext(), "Não foi possível confirmar a entrega.", Toast.LENGTH_SHORT).show();
                            binding.progressBarPedidos.setVisibility(View.GONE);
                        });
                    }
                });
    }

    private void verificarListaVazia() {
        if (pedidoList.isEmpty()) {
            binding.recyclerViewPedidos.setVisibility(View.GONE);
            binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewPedidos.setVisibility(View.VISIBLE);
            binding.textViewNenhumPedido.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}