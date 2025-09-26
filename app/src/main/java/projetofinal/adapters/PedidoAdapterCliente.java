package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.R;
import java.util.List;
import java.util.function.Consumer;
import projetofinal.models.Pedido;

public class PedidoAdapterCliente extends RecyclerView.Adapter<PedidoAdapterCliente.PedidoViewHolder> {

    private final List<Pedido> pedidoList;
    private final Context context;
    private final Consumer<Pedido> onConfirmarEntrega; // Interface para o clique

    public PedidoAdapterCliente(List<Pedido> pedidoList, Context context, Consumer<Pedido> onConfirmarEntrega) {
        this.pedidoList = pedidoList;
        this.context = context;
        this.onConfirmarEntrega = onConfirmarEntrega;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_cliente, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidoList.get(position);
        holder.bind(pedido);
    }

    @Override
    public int getItemCount() {
        return pedidoList.size();
    }

    class PedidoViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewPedidoId;
        private final TextView textViewData;
        private final TextView textViewValor;
        private final TextView textViewDescricao;
        private final TextView textViewStatus;
        private final Button btnConfirmarEntrega; // Botão adicionado

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPedidoId = itemView.findViewById(R.id.textViewPedidoId);
            textViewData = itemView.findViewById(R.id.textViewData);
            textViewValor = itemView.findViewById(R.id.textViewValor);
            textViewDescricao = itemView.findViewById(R.id.textViewDescricao);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            btnConfirmarEntrega = itemView.findViewById(R.id.btnConfirmarEntrega); // Inicializa o botão
        }

        public void bind(Pedido pedido) {
            textViewPedidoId.setText("Pedido #" + pedido.getId());
            textViewData.setText(pedido.getData());
            textViewValor.setText(String.format("R$ %.2f", pedido.getValor()));
            textViewDescricao.setText(pedido.getDescricao().replace("\\n", "\n"));
            textViewStatus.setText("Status: " + capitalize(pedido.getStatus()));

            // Lógica para mostrar o botão
            if ("saiu para entrega".equalsIgnoreCase(pedido.getStatus())) {
                btnConfirmarEntrega.setVisibility(View.VISIBLE);
            } else {
                btnConfirmarEntrega.setVisibility(View.GONE);
            }

            // Ação do clique
            btnConfirmarEntrega.setOnClickListener(v -> {
                onConfirmarEntrega.accept(pedido);
            });
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}