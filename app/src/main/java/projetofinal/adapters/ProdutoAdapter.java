package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projetofinal.R;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import projetofinal.models.Produto;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private List<Produto> produtoList;
    private final OnProdutoInteractionListener listener;
    private final Context context;
    private final String userRole;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_produto, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = produtoList.get(position);
        holder.bind(produto, listener, userRole);
    }

    @Override
    public int getItemCount() {
        return produtoList != null ? produtoList.size() : 0;
    }

    public void atualizarProdutos(List<Produto> novaListaProdutos) {
        this.produtoList.clear();
        if (novaListaProdutos != null) {
            this.produtoList.addAll(novaListaProdutos);
        }
        notifyDataSetChanged();
    }

    class ProdutoViewHolder extends RecyclerView.ViewHolder {
        // IDs Corretos do novo layout
        private final ImageView imgProduto;
        private final TextView txtNomeProduto;
        private final TextView txtDescricaoProduto;
        private final TextView txtPrecoProduto;
        private final RatingBar ratingBarProduto;
        private final TextView txtTotalAvaliacoes;
        private final ImageButton btnExcluirProduto;

        ProdutoViewHolder(View itemView) {
            super(itemView);
            // Mapeando os IDs corretos
            imgProduto = itemView.findViewById(R.id.imgProduto);
            txtNomeProduto = itemView.findViewById(R.id.txtNomeProduto);
            txtDescricaoProduto = itemView.findViewById(R.id.txtDescricaoProduto);
            txtPrecoProduto = itemView.findViewById(R.id.txtPrecoProduto);
            ratingBarProduto = itemView.findViewById(R.id.ratingBarProduto);
            txtTotalAvaliacoes = itemView.findViewById(R.id.txtTotalAvaliacoes);
            btnExcluirProduto = itemView.findViewById(R.id.btnExcluirProduto);
        }

        void bind(final Produto produto, final OnProdutoInteractionListener listener, final String userRole) {
            txtNomeProduto.setText(produto.getNome());
            txtDescricaoProduto.setText(produto.getDescricao());

            if (produto.getPreco() != null) {
                txtPrecoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));
            } else {
                txtPrecoProduto.setText("R$ 0,00");
            }

            // Lógica de avaliação
            ratingBarProduto.setRating((float) produto.getMediaAvaliacoes());
            txtTotalAvaliacoes.setText(String.format(Locale.getDefault(), "(%d)", produto.getTotalAvaliacoes()));

            // Carrega a imagem
            Glide.with(itemView.getContext())
                    .load(produto.getImageUrl())
                    .placeholder(R.drawable.lanchoxburger) // Imagem padrão
                    .error(R.drawable.lanchoxburger) // Imagem de erro
                    .into(imgProduto);

            // Lógica de visibilidade e clique (da sua versão original)
            if (Objects.equals(userRole, "admin")) {
                btnExcluirProduto.setVisibility(View.VISIBLE);
                btnExcluirProduto.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onProdutoDeleteClick(produto);
                    }
                });
                itemView.setOnClickListener(null); // Admin não clica no card
            } else { // Cliente
                btnExcluirProduto.setVisibility(View.GONE);
                // O card inteiro é clicável para o cliente (substitui o antigo btnComprar)
                itemView.setOnClickListener(view -> {
                    if (listener != null) {
                        listener.onProdutoClick(produto, view);
                    }
                });
            }
        }
    }
}