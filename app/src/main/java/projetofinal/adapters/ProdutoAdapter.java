package projetofinal.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.databinding.ItemProdutoBinding;
import java.util.List;
import projetofinal.models.Produto;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private List<Produto> produtoList; // Esta lista é uma referência à lista da Activity
    private final Context context;
    private final OnProdutoInteractionListener listener;
    private static final String TAG = "ProdutoAdapter";

    public interface OnProdutoInteractionListener {
        void onProdutoClick(Produto produto, View clickedView);
    }

    public ProdutoAdapter(Context context, List<Produto> produtoList, OnProdutoInteractionListener listener) {
        this.context = context;
        this.produtoList = produtoList;
        this.listener = listener;
        Log.d(TAG, "Adapter criado. Tamanho inicial da lista: " + (this.produtoList != null ? this.produtoList.size() : "null"));
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
        Log.v(TAG, "Binding produto na posição " + position + ": " + produto.getNome());
        holder.bind(produto, listener);
    }

    @Override
    public int getItemCount() {
        return produtoList != null ? produtoList.size() : 0;
    }

    /**
     * Atualiza a lista de produtos que o adapter exibe.
     * A Activity deve chamar este método após sua lista de dados principal ser modificada.
     * @param novaListaProdutos A nova lista de produtos a ser exibida.
     */
    public void atualizarProdutos(List<Produto> novaListaProdutos) {
        Log.d(TAG, "atualizarProdutos chamado no adapter. Nova lista tem " + (novaListaProdutos != null ? novaListaProdutos.size() : "null") + " itens.");
        notifyDataSetChanged();
        Log.d(TAG, "notifyDataSetChanged() chamado no adapter. Tamanho atual da lista do adapter: " + getItemCount());
    }


    static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        private final ItemProdutoBinding binding;

        ProdutoViewHolder(ItemProdutoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Produto produto, final OnProdutoInteractionListener listener) {
            binding.setProduto(produto);
            binding.setInteractionListener(listener);
            binding.executePendingBindings();
        }
    }
}