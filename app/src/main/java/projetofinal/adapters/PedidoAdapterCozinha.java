package projetofinal.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.R;

import java.util.List;
import java.util.function.Consumer;
import projetofinal.models.Pedido;

public class PedidoAdapterCozinha extends RecyclerView.Adapter<PedidoAdapterCozinha.PedidoViewHolder> {

    private final List<Pedido> pedidoList;
    private final Context context;
    private final Consumer<Pedido> onStatusChange;

    public PedidoAdapterCozinha(List<Pedido> pedidoList, Context context, Consumer<Pedido> onStatusChange) {
        this.pedidoList = pedidoList;
        this.context = context;
        this.onStatusChange = onStatusChange;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_cozinha, parent, false);
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
        private final TextView textViewDescricao;
        private final TextView textViewData;
        private final TextView textViewStatus;
        private final Button btnMudarStatus;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPedidoId = itemView.findViewById(R.id.textViewPedidoId);
            textViewDescricao = itemView.findViewById(R.id.textViewDescricao);
            textViewData = itemView.findViewById(R.id.textViewData);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            btnMudarStatus = itemView.findViewById(R.id.btnMudarStatus);
        }

        public void bind(Pedido pedido) {
            textViewPedidoId.setText("Pedido #" + pedido.getId());
            textViewDescricao.setText(pedido.getDescricao().replace("\\n", "\n"));
            textViewData.setText(pedido.getData());
            textViewStatus.setText(capitalize(pedido.getStatus()));

            updateStatusUI(pedido.getStatus());

            btnMudarStatus.setOnClickListener(v -> {
                onStatusChange.accept(pedido);
            });
        }

        private void updateStatusUI(String status) {
            int backgroundResId;
            String buttonText = "";
            int buttonVisibility = View.VISIBLE;

            switch (status.toLowerCase()) {
                case "pendente":
                    backgroundResId = R.drawable.status_pendente_bg;
                    buttonText = "Iniciar Preparo";
                    break;
                case "em preparo":
                    backgroundResId = R.drawable.status_em_preparo_bg;
                    buttonText = "Finalizar Pedido";
                    break;
                case "saiu para entrega":
                case "concluido":
                    // Itens com esses status não deveriam aparecer, mas por segurança:
                    backgroundResId = R.drawable.status_concluido_bg;
                    buttonVisibility = View.GONE; // Esconde o botão
                    break;
                default:
                    backgroundResId = R.drawable.status_background_placeholder;
                    buttonVisibility = View.GONE; // Esconde o botão para status desconhecido
                    break;
            }

            textViewStatus.setBackground(ContextCompat.getDrawable(context, backgroundResId));
            btnMudarStatus.setText(buttonText);
            btnMudarStatus.setVisibility(buttonVisibility);
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}