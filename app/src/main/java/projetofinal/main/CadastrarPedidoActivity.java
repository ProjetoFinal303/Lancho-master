package projetofinal.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityCadastrarPedidoBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import projetofinal.adapters.CarrinhoAdapter;
import projetofinal.dao.PedidoDao;
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Pedido;
import projetofinal.models.Produto;

public class CadastrarPedidoActivity extends AppCompatActivity implements CarrinhoAdapter.OnCarrinhoInteractionListener {

    private ActivityCadastrarPedidoBinding binding;
    private CarrinhoAdapter carrinhoAdapter;
    private List<CarrinhoItem> itensNoCarrinho = new ArrayList<>();
    private PedidoDao pedidoDao;
    private int clienteIdLogado = -1;
    private Gson gson;

    public static final String CARRINHO_PREFS = "CarrinhoPrefs";
    public static final String KEY_ITENS_CARRINHO = "ItensCarrinho";
    private static final String TAG = "CadastrarPedido";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastrarPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura o Gson para lidar com BigDecimal, que é usado nos modelos
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, typeOfSrc, context) -> src == null ? null : context.serialize(src.toPlainString()));
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonDeserializer<BigDecimal>) (json, typeOfT, context) -> json == null ? null : new BigDecimal(json.getAsString()));
        gson = gsonBuilder.create();

        Toolbar toolbar = binding.toolbarCarrinho;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.meu_carrinho_title));
        }

        pedidoDao = new PedidoDao(this);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (clienteIdLogado == -1) {
            Toast.makeText(this, "Erro: Sessão de cliente inválida. Faça login.", Toast.LENGTH_LONG).show();
            finish(); // Fecha a tela se não houver cliente logado
            return;
        }

        carregarItensCarrinho();
        setupRecyclerView();
        atualizarVisibilidadeCarrinho();
        calcularEAtualizarTotal();

        binding.btnFinalizarPedido.setOnClickListener(v -> finalizarPedido());
    }

    private void setupRecyclerView() {
        binding.recyclerViewItensCarrinho.setLayoutManager(new LinearLayoutManager(this));
        carrinhoAdapter = new CarrinhoAdapter(this, itensNoCarrinho, this);
        binding.recyclerViewItensCarrinho.setAdapter(carrinhoAdapter);
    }

    private void finalizarPedido() {
        if (itensNoCarrinho.isEmpty() || clienteIdLogado == -1) {
            Toast.makeText(this, "Carrinho vazio ou erro de sessão.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        StringBuilder descricao = new StringBuilder();
        BigDecimal valorTotal = BigDecimal.ZERO;
        for(int i = 0; i < itensNoCarrinho.size(); i++) {
            CarrinhoItem item = itensNoCarrinho.get(i);
            descricao.append(item.getQuantidade()).append("x ").append(item.getProduto().getNome());
            if (i < itensNoCarrinho.size() - 1) {
                descricao.append(", ");
            }
            valorTotal = valorTotal.add(item.getPrecoTotalItem());
        }

        Pedido novoPedido = new Pedido(clienteIdLogado, descricao.toString(), valorTotal.doubleValue());

        pedidoDao.inserir(novoPedido,
                // Callback de Sucesso
                response -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Pedido realizado com sucesso!", Toast.LENGTH_LONG).show();
                    itensNoCarrinho.clear();
                    salvarItensCarrinho(); // Limpa o carrinho salvo
                    VisualizarProdutoActivity.setProdutosDesatualizados(true); // Avisa a outra tela para recarregar
                    finish(); // Fecha a tela do carrinho
                }),
                // Callback de Erro
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao finalizar pedido: ", error);
                    Toast.makeText(this, "Falha ao realizar pedido: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarFinalizarPedido.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnFinalizarPedido.setEnabled(!isLoading);
    }

    private void carregarItensCarrinho() {
        if (clienteIdLogado == -1) return;
        SharedPreferences sharedPreferences = getSharedPreferences(CARRINHO_PREFS, Context.MODE_PRIVATE);
        String jsonItens = sharedPreferences.getString(KEY_ITENS_CARRINHO + "_" + clienteIdLogado, null);
        Type type = new TypeToken<ArrayList<CarrinhoItem>>() {}.getType();
        if (jsonItens != null) {
            itensNoCarrinho = gson.fromJson(jsonItens, type);
        }
        if (itensNoCarrinho == null) {
            itensNoCarrinho = new ArrayList<>();
        }
    }

    private void salvarItensCarrinho() {
        if (clienteIdLogado == -1) return;
        SharedPreferences sharedPreferences = getSharedPreferences(CARRINHO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String jsonItens = gson.toJson(itensNoCarrinho);
        editor.putString(KEY_ITENS_CARRINHO + "_" + clienteIdLogado, jsonItens);
        editor.apply();
    }

    private void atualizarVisibilidadeCarrinho() {
        if (itensNoCarrinho.isEmpty()) {
            binding.textViewCarrinhoVazio.setVisibility(View.VISIBLE);
            binding.recyclerViewItensCarrinho.setVisibility(View.GONE);
            binding.btnFinalizarPedido.setEnabled(false);
        } else {
            binding.textViewCarrinhoVazio.setVisibility(View.GONE);
            binding.recyclerViewItensCarrinho.setVisibility(View.VISIBLE);
            binding.btnFinalizarPedido.setEnabled(true);
        }
    }

    private void calcularEAtualizarTotal() {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CarrinhoItem item : itensNoCarrinho) {
            if (item.getPrecoTotalItem() != null) {
                total = total.add(item.getPrecoTotalItem());
            }
        }
        binding.textViewTotalCarrinho.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", total));
    }

    @Override
    public void onItemQuantityChanged() {
        calcularEAtualizarTotal();
        salvarItensCarrinho();
    }

    @Override
    public void onItemRemoved(CarrinhoItem item, int position) {
        if (position >= 0 && position < itensNoCarrinho.size()) {
            itensNoCarrinho.remove(position);
            carrinhoAdapter.notifyItemRemoved(position);
            carrinhoAdapter.notifyItemRangeChanged(position, itensNoCarrinho.size());
            salvarItensCarrinho();
            atualizarVisibilidadeCarrinho();
            calcularEAtualizarTotal();
            Toast.makeText(this, "Item removido: " + item.getProduto().getNome(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}