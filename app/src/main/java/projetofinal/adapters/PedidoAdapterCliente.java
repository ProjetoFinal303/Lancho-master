package projetofinal.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.databinding.ItemPedidoClienteBinding;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import projetofinal.models.Pedido;

public class PedidoAdapterCliente extends RecyclerView.Adapter<PedidoAdapterCliente.PedidoViewHolder> {

    private List<Pedido> pedidoList;
    private final Context context;
    private final boolean isAdminView;

    public PedidoAdapterCliente(Context context, List<Pedido> pedidoList, boolean isAdminView) {
        this.context = context;
        this.pedidoList = pedidoList;
        this.isAdminView = isAdminView;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPedidoClienteBinding binding = ItemPedidoClienteBinding.inflate(inflater, parent, false);
        return new PedidoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidoList.get(position);
        holder.bind(pedido, isAdminView);
    }

    @Override
    public int getItemCount() {
        return pedidoList != null ? pedidoList.size() : 0;
    }

    public void atualizarPedidos(List<Pedido> novosPedidos) {
        this.pedidoList = novosPedidos;
        notifyDataSetChanged();
    }

    class PedidoViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoClienteBinding binding;

        PedidoViewHolder(ItemPedidoClienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Pedido pedido, boolean isAdmin) {
            binding.textViewPedidoIdCliente.setText(String.format(Locale.getDefault(), "Pedido ID: #%d", pedido.getId()));
            binding.textViewPedidoDataCliente.setText(String.format("Data: %s", formatarData(pedido.getData())));
            binding.textViewPedidoDescricaoCliente.setText(pedido.getDescricao());
            binding.textViewPedidoValorCliente.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", pedido.getValor()));

            // AQUI A MUDANÇA: Esconde a label de status
            binding.textViewPedidoStatusCliente.setVisibility(View.GONE);

            if (isAdmin) {
                binding.textViewInfoClienteAdmin.setVisibility(View.VISIBLE);
                binding.textViewInfoClienteAdmin.setText(String.format(Locale.getDefault(), "Cliente ID: %d", pedido.getClienteId()));
            } else {
                binding.textViewInfoClienteAdmin.setVisibility(View.GONE);
            }
        }

        private String formatarData(String dataOriginal) {
            if (dataOriginal == null) return "";
            // Tenta o formato com milissegundos e fuso horário
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            // Tenta o formato sem milissegundos
            SimpleDateFormat formatoEntradaAlternativo = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            // Formato de saída desejado
            SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            try {
                Date date = formatoEntrada.parse(dataOriginal);
                return formatoSaida.format(date);
            } catch (ParseException e) {
                try {
                    Date date = formatoEntradaAlternativo.parse(dataOriginal);
                    return formatoSaida.format(date);
                } catch (ParseException ex) {
                    Log.e("PedidoAdapter", "Erro ao parsear data: " + dataOriginal, ex);
                    return dataOriginal.split("T")[0]; // Em último caso, retorna só a data
                }
            }
        }
    }
}