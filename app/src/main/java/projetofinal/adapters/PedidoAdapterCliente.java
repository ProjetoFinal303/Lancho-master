package projetofinal.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ItemPedidoClienteBinding;
import java.util.List;
import java.util.Locale;
import projetofinal.models.Pedido;

public class PedidoAdapterCliente extends RecyclerView.Adapter<PedidoAdapterCliente.PedidoViewHolder> {

    private List<Pedido> pedidoList;
    private final Context context;

    public PedidoAdapterCliente(Context context, List<Pedido> pedidoList) {
        this.context = context;
        this.pedidoList = pedidoList;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPedidoClienteBinding binding = ItemPedidoClienteBinding.inflate(inflater, parent, false);
        return new PedidoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidoList.get(position);
        holder.bind(pedido);
    }

    @Override
    public int getItemCount() {
        return pedidoList != null ? pedidoList.size() : 0;
    }

    public void atualizarPedidos(List<Pedido> novosPedidos) {
        this.pedidoList = novosPedidos;
        notifyDataSetChanged(); // Para simplicidade. Considere DiffUtil para listas grandes.
    }

    class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoClienteBinding binding;

        PedidoViewHolder(ItemPedidoClienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Pedido pedido) {
            binding.textViewPedidoIdCliente.setText(String.format(Locale.getDefault(), "Pedido ID: #%d", pedido.getId()));
            binding.textViewPedidoDataCliente.setText(String.format("Data: %s", pedido.getData())); // Formate a data se necessário
            binding.textViewPedidoDescricaoCliente.setText(pedido.getDescricao());
            binding.textViewPedidoValorCliente.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", pedido.getValor()));
            binding.textViewPedidoStatusCliente.setText(pedido.getStatus());

            // Mudar a cor de fundo do status
            GradientDrawable statusBackground = (GradientDrawable) binding.textViewPedidoStatusCliente.getBackground();
            if (statusBackground == null) {
                statusBackground = new GradientDrawable();
                statusBackground.setCornerRadius(context.getResources().getDimensionPixelSize(R.dimen.status_corner_radius));
                binding.textViewPedidoStatusCliente.setBackground(statusBackground);
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
                case "concluido": // Adicionando variação sem acento
                    colorResId = R.color.status_concluido;
                    break;
                case "cancelado":
                    colorResId = R.color.status_cancelado;
                    break;
                default:
                    colorResId = android.R.color.darker_gray; // Cor padrão
                    break;
            }
            statusBackground.setColor(ContextCompat.getColor(context, colorResId));
        }
    }
}
