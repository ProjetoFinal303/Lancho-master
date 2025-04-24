package projetofinal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import projetofinal.models.Cliente;
import com.example.projetofinal.R;

import java.util.List;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClientViewHolder> {

    private List<Cliente> clienteList;
    private Context context;

    // Construtor do adaptador (passando o Context junto com a lista de clientes)
    public ClienteAdapter(Context context, List<Cliente> clienteList) {
        this.context = context; // Salvando o contexto
        this.clienteList = clienteList;
    }

    // Criação do ViewHolder
    @Override
    public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Transforma o layout customizado para cada item do RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ClientViewHolder(view);
    }

    //dados do cliente para preencher o ViewHolder
    @Override
    public void onBindViewHolder(ClientViewHolder holder, int position) {
        Cliente cliente = clienteList.get(position);
        holder.nameTextView.setText(cliente.getNome());
        holder.contactTextView.setText(cliente.getContato());

        // Atualizando o TextView do ID com o valor do cliente
        holder.idTextView.setText("ID: " + cliente.getId()); 
    }

    // Retorna a quantidade de clientes na lista
    @Override
    public int getItemCount() {
        return clienteList.size();
    }

    // ViewHolder que armazena as referências de cada item
    public static class ClientViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView contactTextView;
        TextView idTextView; 

        public ClientViewHolder(View itemView) {
            super(itemView);
            // Inicializa as views do layout customizado
            nameTextView = itemView.findViewById(R.id.text_cliente_nome);
            contactTextView = itemView.findViewById(R.id.text_cliente_contato);
            idTextView = itemView.findViewById(R.id.text_cliente_id); 
        }
    }

    // Metodo para atualizar a lista de clientes no adaptador
    public void updateClientList(List<Cliente> newClientList) {
        this.clienteList = newClientList;
        notifyDataSetChanged();
    }
}
