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
    private final Consumer<Pedido> onConfirmarEntrega;
    private final Consumer<Pedido> onAvaliarPedido; // Novo listener para o botão de avaliar

    // Construtor atualizado
    public PedidoAdapterCliente(List<Pedido> pedidoList, Context context, Consumer<Pedido> onConfirmarEntrega, Consumer<Pedido> onAvaliarPedido) {
        this.pedidoList = pedidoList;
        this.context = context;
        this.onConfirmarEntrega = onConfirmarEntrega;
        this.onAvaliarPedido = onAvaliarPedido;
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
        private final Button btnConfirmarEntrega;
        private final Button btnAvaliarPedido; // Referência para o novo botão

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPedidoId = itemView.findViewById(R.id.textViewPedidoId);
            textViewData = itemView.findViewById(R.id.textViewData);
            textViewValor = itemView.findViewById(R.id.textViewValor);
            textViewDescricao = itemView.findViewById(R.id.textViewDescricao);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            btnConfirmarEntrega = itemView.findViewById(R.id.btnConfirmarEntrega);
            btnAvaliarPedido = itemView.findViewById(R.id.btnAvaliarPedido); // Inicializa o novo botão
        }

        public void bind(Pedido pedido) {
            textViewPedidoId.setText("Pedido #" + pedido.getId());
            textViewData.setText(pedido.getData());
            textViewValor.setText(String.format("R$ %.2f", pedido.getValor()));
            textViewDescricao.setText(pedido.getDescricao().replace("\\n", "\n"));
            textViewStatus.setText("Status: " + capitalize(pedido.getStatus()));

            // Lógica de visibilidade dos botões
            btnConfirmarEntrega.setVisibility(View.GONE);
            btnAvaliarPedido.setVisibility(View.GONE);

            String status = pedido.getStatus().toLowerCase();
            if ("saiu para entrega".equals(status)) {
                btnConfirmarEntrega.setVisibility(View.VISIBLE);
            } else if ("concluido".equals(status)) {
                btnAvaliarPedido.setVisibility(View.VISIBLE);
            }

            // Ações de clique
            btnConfirmarEntrega.setOnClickListener(v -> onConfirmarEntrega.accept(pedido));
            btnAvaliarPedido.setOnClickListener(v -> onAvaliarPedido.accept(pedido));
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}