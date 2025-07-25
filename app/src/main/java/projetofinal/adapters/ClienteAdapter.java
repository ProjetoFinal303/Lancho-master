package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.databinding.ItemClienteBinding;
import java.util.List;
import projetofinal.models.Cliente;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClientViewHolder> {

    private List<Cliente> clienteList;
    private final OnItemClickListener listener;

    // Interface para lidar com cliques no item e no botão de excluir
    public interface OnItemClickListener {
        void onItemClick(Cliente cliente);
        void onDeleteClick(Cliente cliente); // Novo método para o clique de exclusão
    }

    public ClienteAdapter(List<Cliente> clienteList, OnItemClickListener listener) {
        this.clienteList = clienteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemClienteBinding binding = ItemClienteBinding.inflate(inflater, parent, false);
        return new ClientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Cliente cliente = clienteList.get(position);
        holder.bind(cliente, listener);
    }

    @Override
    public int getItemCount() {
        return clienteList != null ? clienteList.size() : 0;
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        ItemClienteBinding binding;

        public ClientViewHolder(ItemClienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Cliente cliente, final OnItemClickListener listener) {
            binding.textClienteId.setText("ID: " + cliente.getId());
            binding.textClienteNome.setText(cliente.getNome());
            binding.textClienteContato.setText("Contato: " + cliente.getContato());
            binding.textClienteEmail.setText("Email: " + cliente.getEmail());

            itemView.setOnClickListener(v -> listener.onItemClick(cliente));

            // Configura o clique para o botão de excluir
            binding.btnExcluirCliente.setOnClickListener(v -> listener.onDeleteClick(cliente));
        }
    }

    public void updateClientList(List<Cliente> newClientList) {
        this.clienteList.clear();
        if (newClientList != null) {
            this.clienteList.addAll(newClientList);
        }
        notifyDataSetChanged();
    }
}