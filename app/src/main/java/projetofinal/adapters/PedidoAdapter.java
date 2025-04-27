package projetofinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.R;

import java.util.List;

import projetofinal.models.Pedido;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos;

    public PedidoAdapter(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.textDescricao.setText(pedido.getDescricao());
        holder.textValor.setText(String.format("R$ %.2f", pedido.getValor()));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView textDescricao, textValor;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescricao = itemView.findViewById(R.id.textDescricaoPedido);
            textValor = itemView.findViewById(R.id.textValorPedido);
        }
    }
}
