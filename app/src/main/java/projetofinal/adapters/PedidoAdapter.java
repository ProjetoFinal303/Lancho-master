package projetofinal.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.R; // Certifique-se que R é importado corretamente
import com.example.projetofinal.databinding.ItemPedidoBinding; // Binding para item_pedido.xml
import java.util.List;
import java.util.Locale;
import projetofinal.models.Pedido;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<Pedido> pedidoList;
    private final Context context;

    public PedidoAdapter(Context context, List<Pedido> pedidoList /*, OnPedidoClickListener listener */) {
        this.context = context;
        this.pedidoList = pedidoList;
        // this.clickListener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // Inflar usando a classe de binding gerada para item_pedido.xml
        ItemPedidoBinding binding = ItemPedidoBinding.inflate(inflater, parent, false);
        return new PedidoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidoList.get(position);
        holder.bind(pedido /*, clickListener */);
    }

    @Override
    public int getItemCount() {
        return pedidoList != null ? pedidoList.size() : 0;
    }

    public void atualizarPedidos(List<Pedido> novosPedidos) {
        this.pedidoList = novosPedidos;
        notifyDataSetChanged(); // Para simplicidade. Considere DiffUtil para listas grandes.
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoBinding binding; // Usar o binding

        PedidoViewHolder(ItemPedidoBinding binding) {
            super(binding.getRoot());
            this.binding = binding; // Armazenar o binding
        }

        void bind(final Pedido pedido /*, final OnPedidoClickListener listener */) {
            // Acessar views através do objeto binding
            binding.textViewPedidoIdGenerico.setText(String.format(Locale.getDefault(), "Pedido ID: #%d", pedido.getId()));
            binding.textViewDataPedidoGenerico.setText(String.format("Data: %s", pedido.getData()));
            binding.textDescricaoPedido.setText(pedido.getDescricao()); // ID corrigido no XML
            binding.textValorPedido.setText(String.format(Locale.getDefault(), "Valor Total: R$ %.2f", pedido.getValor())); // ID corrigido no XML
            binding.textViewStatusPedidoGenerico.setText(String.format("Status: %s", pedido.getStatus()));

            // Lógica para colorir o status (opcional, mas melhora a UI)
            GradientDrawable statusBackground = (GradientDrawable) binding.textViewStatusPedidoGenerico.getBackground();
            if (statusBackground == null) { // Cria um se não existir no XML (melhor ter um drawable base como status_background_placeholder)
                statusBackground = new GradientDrawable();
                statusBackground.setCornerRadius(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.status_corner_radius));
                binding.textViewStatusPedidoGenerico.setBackground(statusBackground);
            }

            int colorResId;
            switch (pedido.getStatus().toLowerCase(Locale.ROOT)) {
                case "pendente":
                    colorResId = R.color.status_pendente;
                    break;
                case "em preparo":
                    colorResId = R.color.status_em_preparo;
                    break;
                case "concluído":
                case "concluido":
                    colorResId = R.color.status_concluido;
                    break;
                case "cancelado":
                    colorResId = R.color.status_cancelado;
                    break;
                default:
                    colorResId = android.R.color.darker_gray;
                    break;
            }
            statusBackground.setColor(ContextCompat.getColor(itemView.getContext(), colorResId));


            // Se você tiver um listener para cliques no item:
            // itemView.setOnClickListener(v -> {
            //    if (listener != null) {
            //        listener.onPedidoClick(pedido);
            //    }
            // });
        }
    }
}
