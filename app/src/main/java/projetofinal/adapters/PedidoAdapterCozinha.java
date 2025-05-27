package projetofinal.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ItemPedidoCozinhaBinding;
import java.util.List;
import java.util.Locale;
import projetofinal.models.Pedido;

public class PedidoAdapterCozinha extends RecyclerView.Adapter<PedidoAdapterCozinha.ComandaViewHolder> {

    private List<Pedido> comandaList;
    private final Context context;
    private final OnComandaInteractionListener listener;

    public interface OnComandaInteractionListener {
        void onStatusChangeClicked(Pedido pedido, String novoStatus);
    }

    public PedidoAdapterCozinha(Context context, List<Pedido> comandaList, OnComandaInteractionListener listener) {
        this.context = context;
        this.comandaList = comandaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComandaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPedidoCozinhaBinding binding = ItemPedidoCozinhaBinding.inflate(inflater, parent, false);
        return new ComandaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ComandaViewHolder holder, int position) {
        Pedido comanda = comandaList.get(position);
        holder.bind(comanda);
    }

    @Override
    public int getItemCount() {
        return comandaList != null ? comandaList.size() : 0;
    }

    public void atualizarComandas(List<Pedido> novasComandas) {
        this.comandaList = novasComandas;
        notifyDataSetChanged();
    }

    class ComandaViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoCozinhaBinding binding;

        ComandaViewHolder(ItemPedidoCozinhaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnMarcarEmPreparo.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onStatusChangeClicked(comandaList.get(position), "Em Preparo");
                }
            });

            binding.btnMarcarConcluido.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onStatusChangeClicked(comandaList.get(position), "Concluído");
                }
            });
        }

        void bind(Pedido comanda) {
            binding.textViewPedidoIdCozinha.setText(String.format(Locale.getDefault(), "Comanda ID: #%d", comanda.getId()));
            binding.textViewPedidoClienteInfoCozinha.setText(String.format(Locale.getDefault(), "Cliente ID: %d", comanda.getClienteId()));
            binding.textViewPedidoDataCozinha.setText(String.format("Recebido: %s", comanda.getData()));
            binding.textViewPedidoDescricaoCozinha.setText(comanda.getDescricao());
            binding.textViewPedidoStatusAtualCozinha.setText(String.format("Status: %s", comanda.getStatus()));

            // Visibilidade dos botões baseada no status atual
            if ("Pendente".equalsIgnoreCase(comanda.getStatus())) {
                binding.btnMarcarEmPreparo.setVisibility(View.VISIBLE);
                binding.btnMarcarConcluido.setVisibility(View.GONE);
            } else if ("Em Preparo".equalsIgnoreCase(comanda.getStatus())) {
                binding.btnMarcarEmPreparo.setVisibility(View.GONE);
                binding.btnMarcarConcluido.setVisibility(View.VISIBLE);
            } else { // Concluído ou Cancelado
                binding.btnMarcarEmPreparo.setVisibility(View.GONE);
                binding.btnMarcarConcluido.setVisibility(View.GONE);
            }

            GradientDrawable statusBackground = (GradientDrawable) binding.textViewPedidoStatusAtualCozinha.getBackground();
            if (statusBackground == null) {
                statusBackground = new GradientDrawable();
                statusBackground.setCornerRadius(context.getResources().getDimensionPixelSize(R.dimen.status_corner_radius));
                binding.textViewPedidoStatusAtualCozinha.setBackground(statusBackground);
            }

            int colorResId;
            switch (comanda.getStatus().toLowerCase(Locale.ROOT)) {
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
            statusBackground.setColor(ContextCompat.getColor(context, colorResId));
        }
    }
}
