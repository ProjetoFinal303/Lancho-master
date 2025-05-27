package projetofinal.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.R; // Certifique-se que R é importado corretamente
import com.example.projetofinal.databinding.ItemCarrinhoProdutoBinding;

import java.util.List;
import java.util.Locale;

import projetofinal.models.CarrinhoItem;

public class CarrinhoAdapter extends RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder> {

    private List<CarrinhoItem> itensCarrinho;
    private final Context context;
    private final OnCarrinhoInteractionListener listener;

    public interface OnCarrinhoInteractionListener {
        void onItemQuantityChanged(); // Chamado quando a quantidade muda para atualizar o total
        void onItemRemoved(CarrinhoItem item, int position); // Chamado para remover o item
    }

    public CarrinhoAdapter(Context context, List<CarrinhoItem> itensCarrinho, OnCarrinhoInteractionListener listener) {
        this.context = context;
        this.itensCarrinho = itensCarrinho;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarrinhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCarrinhoProdutoBinding binding = ItemCarrinhoProdutoBinding.inflate(inflater, parent, false);
        return new CarrinhoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CarrinhoViewHolder holder, int position) {
        CarrinhoItem item = itensCarrinho.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itensCarrinho != null ? itensCarrinho.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void atualizarItens(List<CarrinhoItem> novosItens) {
        this.itensCarrinho = novosItens;
        notifyDataSetChanged(); // Para simplicidade.
    }


    class CarrinhoViewHolder extends RecyclerView.ViewHolder {
        private final ItemCarrinhoProdutoBinding binding;

        CarrinhoViewHolder(ItemCarrinhoProdutoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnAumentarQuantidade.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CarrinhoItem item = itensCarrinho.get(position);
                    item.incrementarQuantidade();
                    notifyItemChanged(position);
                    listener.onItemQuantityChanged();
                }
            });

            binding.btnDiminuirQuantidade.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CarrinhoItem item = itensCarrinho.get(position);
                    if (item.getQuantidade() > 1) { // Não permite zerar por aqui, só remover
                        item.decrementarQuantidade();
                        notifyItemChanged(position);
                    } else {
                        // Se a quantidade for 1, o próximo clique em diminuir poderia remover o item
                        // ou mostrar um aviso. Para remover, use o botão de remover.
                        Toast.makeText(context, "Use o botão remover para tirar o item.", Toast.LENGTH_SHORT).show();
                    }
                    listener.onItemQuantityChanged();
                }
            });

            binding.btnRemoverItemCarrinho.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CarrinhoItem itemRemovido = itensCarrinho.get(position);
                    listener.onItemRemoved(itemRemovido, position);
                    // listener.onItemQuantityChanged(); // Atualizar total após remoção
                }
            });
        }

        void bind(CarrinhoItem item) {
            binding.textViewNomeProdutoCarrinho.setText(item.getProduto().getNome());
            binding.textViewPrecoUnitarioCarrinho.setText(String.format(Locale.getDefault(), "Unid: R$ %.2f", item.getProduto().getPreco()));
            binding.textViewQuantidadeCarrinho.setText(String.valueOf(item.getQuantidade()));
            binding.textViewPrecoTotalItemCarrinho.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getPrecoTotalItem()));
        }
    }
}
