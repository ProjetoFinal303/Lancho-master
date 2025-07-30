package projetofinal.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.databinding.FragmentPedidosBinding;
import java.util.ArrayList;
import java.util.List;
import projetofinal.adapters.PedidoAdapterCliente;
import projetofinal.dao.PedidoDao;
import projetofinal.models.Pedido;

public class PedidosFragment extends Fragment {

    private FragmentPedidosBinding binding;
    private PedidoDao pedidoDao;
    private PedidoAdapterCliente pedidoAdapter;
    private List<Pedido> listaDePedidos = new ArrayList<>();
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

        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (clienteId != -1) {
            carregarPedidosDoCliente(clienteId);
        } else {
            binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
            binding.progressBarPedidos.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        binding.recyclerViewPedidos.setLayoutManager(new LinearLayoutManager(getContext()));
        pedidoAdapter = new PedidoAdapterCliente(getContext(), listaDePedidos, false);
        binding.recyclerViewPedidos.setAdapter(pedidoAdapter);
    }

    private void carregarPedidosDoCliente(int idCliente) {
        binding.progressBarPedidos.setVisibility(View.VISIBLE);
        pedidoDao.buscarPedidosPorClienteId(idCliente,
                pedidos -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.progressBarPedidos.setVisibility(View.GONE);
                            if (pedidos != null && !pedidos.isEmpty()) {
                                listaDePedidos.clear();
                                listaDePedidos.addAll(pedidos);
                                listaDePedidos.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                                pedidoAdapter.atualizarPedidos(listaDePedidos);
                                binding.recyclerViewPedidos.setVisibility(View.VISIBLE);
                                binding.textViewNenhumPedido.setVisibility(View.GONE);
                            } else {
                                binding.recyclerViewPedidos.setVisibility(View.GONE);
                                binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                },
                error -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.progressBarPedidos.setVisibility(View.GONE);
                            Log.e(TAG, "Erro ao carregar pedidos: ", error);
                            binding.textViewNenhumPedido.setVisibility(View.VISIBLE);
                        });
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}