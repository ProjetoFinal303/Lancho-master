package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.databinding.ItemEstoqueBinding;
import java.util.List;
import java.util.Locale;
import projetofinal.models.Estoque;

public class EstoqueAdapter extends RecyclerView.Adapter<EstoqueAdapter.EstoqueViewHolder> {

    private List<Estoque> estoqueList;
    private final Context context;
    private final OnEstoqueItemClickListener listener;

    public interface OnEstoqueItemClickListener {
        void onItemClick(Estoque estoque);
    }

    public EstoqueAdapter(Context context, List<Estoque> estoqueList, OnEstoqueItemClickListener listener) {
        this.context = context;
        this.estoqueList = estoqueList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EstoqueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEstoqueBinding binding = ItemEstoqueBinding.inflate(inflater, parent, false);
        return new EstoqueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EstoqueViewHolder holder, int position) {
        Estoque estoque = estoqueList.get(position);
        holder.bind(estoque, listener);
    }

    @Override
    public int getItemCount() {
        return estoqueList != null ? estoqueList.size() : 0;
    }

    public void updateEstoqueList(List<Estoque> newEstoqueList) {
        this.estoqueList.clear();
        if (newEstoqueList != null) {
            this.estoqueList.addAll(newEstoqueList);
        }
        notifyDataSetChanged(); // Para simplicidade. Considere DiffUtil.
    }

    static class EstoqueViewHolder extends RecyclerView.ViewHolder {
        private final ItemEstoqueBinding binding;

        public EstoqueViewHolder(ItemEstoqueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Estoque estoque, final OnEstoqueItemClickListener listener) {
            binding.textNomeProdutoEstoque.setText(estoque.getNomeProduto() != null ? estoque.getNomeProduto() : "Produto Desconhecido");
            binding.textProdutoIdEstoque.setText(String.format(Locale.getDefault(), "ID Produto: %d", estoque.getProdutoId()));
            binding.textQuantidadeEstoque.setText(String.format(Locale.getDefault(), "Em estoque: %d unidades", estoque.getQuantidade()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(estoque);
                }
            });
        }
    }
}
