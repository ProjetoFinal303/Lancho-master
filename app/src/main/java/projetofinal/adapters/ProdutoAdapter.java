package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Importar a classe Button
import android.widget.ImageView;
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
    private final String userRole; // Para saber se é admin ou cliente

    public interface OnProdutoInteractionListener {
        void onProdutoClick(Produto produto, View clickedView);
    }

    public ProdutoAdapter(Context context, List<Produto> produtoList, OnProdutoInteractionListener listener, String userRole) {
        this.context = context;
        this.produtoList = produtoList;
        this.listener = listener;
        this.userRole = userRole; // Armazena o tipo de usuário
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
        private final ImageView imagemProduto;
        private final TextView nomeProduto;
        private final TextView descricaoProduto;
        private final TextView precoProduto;
        private final Button btnComprar; // Referência para o novo botão

        ProdutoViewHolder(View itemView) {
            super(itemView);
            imagemProduto = itemView.findViewById(R.id.imagemProduto);
            nomeProduto = itemView.findViewById(R.id.nomeProduto);
            descricaoProduto = itemView.findViewById(R.id.descricaoProduto);
            precoProduto = itemView.findViewById(R.id.precoProduto);
            btnComprar = itemView.findViewById(R.id.btnComprar); // Mapeia o botão
        }

        void bind(final Produto produto, final OnProdutoInteractionListener listener, final String userRole) {
            nomeProduto.setText(produto.getNome());
            descricaoProduto.setText(produto.getDescricao());
            if (produto.getPreco() != null) {
                precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));
            } else {
                precoProduto.setText("R$ 0,00");
            }

            if (produto.getImageUrl() != null && !produto.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(produto.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_background)
                        .into(imagemProduto);
            } else {
                imagemProduto.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // LÓGICA DO BOTÃO
            // Se o usuário for admin, o botão fica escondido
            if (Objects.equals(userRole, "admin")) {
                btnComprar.setVisibility(View.GONE);
            } else {
                // Se for cliente, o botão fica visível e com a ação de clique
                btnComprar.setVisibility(View.VISIBLE);
                btnComprar.setOnClickListener(view -> {
                    if (listener != null) {
                        listener.onProdutoClick(produto, view);
                    }
                });
            }
        }
    }
}