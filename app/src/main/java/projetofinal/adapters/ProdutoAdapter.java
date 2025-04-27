package projetofinal.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.databinding.ItemProdutoBinding;

import java.util.List;

import projetofinal.models.Produto;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private final List<Produto> produtoList;

    public ProdutoAdapter(List<Produto> produtoList) {
        this.produtoList = produtoList;
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProdutoBinding binding = ItemProdutoBinding.inflate(inflater, parent, false);
        return new ProdutoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = produtoList.get(position);
        holder.binding.setProduto(produto); // Aqui entra o DataBinding automático
    }

    @Override
    public int getItemCount() {
        return produtoList.size();
    }

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        final ItemProdutoBinding binding;

        public ProdutoViewHolder(ItemProdutoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
