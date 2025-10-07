package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ItemProdutoBinding;

import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import projetofinal.models.Produto;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private final List<Produto> produtoList;
    private final OnProdutoInteractionListener listener;
    private final String userRole;
    private final Context context;

    public interface OnProdutoInteractionListener {
        void onProdutoClick(Produto produto, View clickedView);
        void onProdutoDeleteClick(Produto produto);
    }

    public ProdutoAdapter(Context context, List<Produto> produtoList, OnProdutoInteractionListener listener, String userRole) {
        this.context = context;
        this.produtoList = produtoList;
        this.listener = listener;
        this.userRole = userRole;
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
        holder.bind(produto, listener, userRole, context);
    }

    @Override
    public int getItemCount() {
        return produtoList.size();
    }

    public void atualizarProdutos(List<Produto> novosProdutos) {
        this.produtoList.clear();
        if (novosProdutos != null) {
            this.produtoList.addAll(novosProdutos);
        }
        notifyDataSetChanged();
    }

    static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        private final ItemProdutoBinding binding;

        public ProdutoViewHolder(ItemProdutoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Produto produto, final OnProdutoInteractionListener listener, String userRole, Context context) {
            binding.txtNomeProduto.setText(produto.getNome());
            binding.txtDescricaoProduto.setText(produto.getDescricao());

            if (produto.getPreco() != null) {
                String precoFormatado = String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco().setScale(2, RoundingMode.HALF_UP));
                binding.txtPrecoProduto.setText(precoFormatado);
            }

            Glide.with(context)
                    .load(produto.getImageUrl())
                    .placeholder(R.drawable.lanchoxburger)
                    .error(R.drawable.lanchoxburger)
                    .into(binding.imgProduto);

            binding.ratingBarProduto.setRating((float) produto.getMediaAvaliacoes());
            binding.txtTotalAvaliacoes.setText(String.format(Locale.getDefault(), "(%d)", produto.getTotalAvaliacoes()));

            // O ID AQUI ESTÁ CORRIGIDO PARA BINDING.TXTESTOQUEPRODUTO
            binding.txtEstoqueProduto.setText(String.format(Locale.getDefault(), "Estoque: %d", produto.getQuantidadeEstoque()));

            if ("admin".equals(userRole)) {
                binding.btnExcluirProduto.setVisibility(View.VISIBLE);
                binding.btnExcluirProduto.setOnClickListener(v -> listener.onProdutoDeleteClick(produto));
                itemView.setOnClickListener(v -> listener.onProdutoClick(produto, itemView));
            } else {
                binding.btnExcluirProduto.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> listener.onProdutoClick(produto, itemView));
            }
        }
    }
}