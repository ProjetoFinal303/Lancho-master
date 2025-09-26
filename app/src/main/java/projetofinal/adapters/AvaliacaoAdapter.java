package projetofinal.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import projetofinal.models.Avaliacao;
import projetofinal.models.Produto;

public class AvaliacaoAdapter extends RecyclerView.Adapter<AvaliacaoAdapter.AvaliacaoViewHolder> {

    private final Context context;
    private final List<Produto> produtoList;
    // Usamos um Map para guardar as avaliações em andamento, associadas ao ID do produto
    private final Map<Integer, Avaliacao> avaliacoesMap = new HashMap<>();

    public AvaliacaoAdapter(Context context, List<Produto> produtoList) {
        this.context = context;
        this.produtoList = produtoList;
    }

    @NonNull
    @Override
    public AvaliacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avaliacao_produto, parent, false);
        return new AvaliacaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvaliacaoViewHolder holder, int position) {
        Produto produto = produtoList.get(position);
        holder.bind(produto);
    }

    @Override
    public int getItemCount() {
        return produtoList.size();
    }

    // Método para a Activity pegar todas as avaliações preenchidas
    public List<Avaliacao> getAvaliacoes() {
        // Retorna apenas as avaliações que receberam uma nota
        List<Avaliacao> avaliacoesValidas = new ArrayList<>();
        for (Avaliacao avaliacao : avaliacoesMap.values()) {
            if (avaliacao.getNota() > 0) {
                avaliacoesValidas.add(avaliacao);
            }
        }
        return avaliacoesValidas;
    }

    class AvaliacaoViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewNomeProduto;
        private final RatingBar ratingBar;
        private final EditText editTextComentario;

        public AvaliacaoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNomeProduto = itemView.findViewById(R.id.textViewNomeProdutoAvaliacao);
            ratingBar = itemView.findViewById(R.id.ratingBarItemAvaliacao);
            editTextComentario = itemView.findViewById(R.id.editTextComentario);
        }

        public void bind(Produto produto) {
            textViewNomeProduto.setText(produto.getNome());

            // Garante que o estado (nota/comentário) seja mantido se a lista rolar
            Avaliacao avaliacaoAtual = avaliacoesMap.get(produto.getId());
            if (avaliacaoAtual != null) {
                ratingBar.setRating(avaliacaoAtual.getNota());
                editTextComentario.setText(avaliacaoAtual.getComentario());
            } else {
                ratingBar.setRating(0);
                editTextComentario.setText("");
            }

            // Listener para quando a nota mudar
            ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    int produtoId = produtoList.get(getAdapterPosition()).getId();
                    Avaliacao avaliacao = avaliacoesMap.get(produtoId);
                    if (avaliacao == null) {
                        avaliacao = new Avaliacao(produtoId, 0, (int) rating, "");
                        avaliacoesMap.put(produtoId, avaliacao);
                    }
                    avaliacao.setNota((int) rating);
                }
            });

            // Listener para quando o texto do comentário mudar
            editTextComentario.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int produtoId = produtoList.get(getAdapterPosition()).getId();
                    Avaliacao avaliacao = avaliacoesMap.get(produtoId);
                    if (avaliacao == null) {
                        // Cria uma avaliação com nota 0 se o usuário só digitar texto
                        avaliacao = new Avaliacao(produtoId, 0, 0, s.toString());
                        avaliacoesMap.put(produtoId, avaliacao);
                    }
                    avaliacao.setComentario(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
}