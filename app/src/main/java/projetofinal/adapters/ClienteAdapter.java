package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import projetofinal.models.Cliente;
import com.example.projetofinal.databinding.ItemClienteBinding;

import java.util.List;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClientViewHolder> {

    private List<Cliente> clienteList;
    private Context context;

    // Construtor do adaptador
    public ClienteAdapter(Context context, List<Cliente> clienteList) {
        this.context = context;
        this.clienteList = clienteList;
    }

    // Criação do ViewHolder com ViewBinding
    @Override
    public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemClienteBinding binding = ItemClienteBinding.inflate(inflater, parent, false);
        return new ClientViewHolder(binding);
    }

    // Preenchimento dos dados no ViewHolder
    @Override
    public void onBindViewHolder(ClientViewHolder holder, int position) {
        Cliente cliente = clienteList.get(position);
        holder.binding.textClienteNome.setText(cliente.getNome());
        holder.binding.textClienteContato.setText(cliente.getContato());
        holder.binding.textClienteId.setText("ID: " + cliente.getId());
    }

    @Override
    public int getItemCount() {
        return clienteList != null ? clienteList.size() : 0;
    }

    // ViewHolder atualizado com ViewBinding
    public static class ClientViewHolder extends RecyclerView.ViewHolder {

        ItemClienteBinding binding;

        public ClientViewHolder(ItemClienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // Atualização da lista de clientes com a nova lista
    public void updateClientList(List<Cliente> newClientList) {
        if (newClientList != null) {
            this.clienteList = newClientList;
            notifyDataSetChanged();
        }
    }

    // Método para obter um cliente baseado na posição
    public Cliente getItemAtPosition(int position) {
        return clienteList != null && position >= 0 && position < clienteList.size() ? clienteList.get(position) : null;
    }
}
