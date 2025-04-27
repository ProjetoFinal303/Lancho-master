package projetofinal.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetofinal.R;

import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class MainClienteActivity extends AppCompatActivity {

    private ClienteDao clienteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cliente);

        // Inicializar ClienteDao
        clienteDao = new ClienteDao(this); // Passando o contexto para o ClienteDao


        // Obter o cliente logado (supondo que tenha um método para obter o cliente logado)
        Cliente clienteLogado = getClienteLogado();

        // Verificar se o cliente está logado
        if (clienteLogado == null) {
            Toast.makeText(this, "Erro ao obter cliente logado!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Botão para visualizar produtos
        findViewById(R.id.btnVisualizarProdutos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainClienteActivity.this, VisualizarProdutoActivity.class));
            }
        });

        // Botão para visualizar os pedidos do cliente logado
        findViewById(R.id.btnVisualizarMeusPedidos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainClienteActivity.this, VisualizarPedidosActivity.class);
                intent.putExtra("cliente_id", clienteLogado.getId()); // Passando o id do cliente logado
                startActivity(intent);
            }
        });
    }

    // Método para obter o cliente logado. (Você pode adaptar isso conforme o seu fluxo de login)
    private Cliente getClienteLogado() {
        // Abaixo, exemplo de como recuperar o cliente logado usando um banco de dados.
        // Aqui, você pode obter o cliente logado da maneira que você já implementou (SharedPreferences, etc).
        // Exemplo de busca por email no banco de dados:
        return clienteDao.buscarPorEmail("email_cliente_logado@example.com"); // Substitua por seu método de login
    }
}
