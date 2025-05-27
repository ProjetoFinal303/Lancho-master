package projetofinal.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.gson.GsonBuilder; // Import GsonBuilder
import com.google.gson.JsonDeserializer; // Import para deserializer
import com.google.gson.JsonSerializer;   // Import para serializer
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal; // Import BigDecimal
import java.math.RoundingMode; // Import RoundingMode
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import projetofinal.adapters.CarrinhoAdapter;
import projetofinal.dao.PedidoDao;
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Pedido;
import projetofinal.models.Produto;

public class CadastrarPedidoActivity extends AppCompatActivity implements CarrinhoAdapter.OnCarrinhoInteractionListener {

    private ActivityCadastrarPedidoBinding binding;
    private CarrinhoAdapter carrinhoAdapter;
    private List<CarrinhoItem> itensNoCarrinho;
    private PedidoDao pedidoDao;
    private ExecutorService executorService;
    private int clienteIdLogado = -1;
    private Gson gson;

    public static final String EXTRA_PRODUTO_ADICIONAR_CARRINHO = "EXTRA_PRODUTO_ADICIONAR_CARRINHO";
    public static final String CARRINHO_PREFS = "CarrinhoPrefs";
    public static final String KEY_ITENS_CARRINHO = "ItensCarrinho";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastrarPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        String userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");

        if (clienteIdLogado == -1 || !"cliente".equals(userRole)) {
            Toast.makeText(this, "Erro: Sessão de cliente inválida. Faça login.", Toast.LENGTH_LONG).show();
        }

        carregarItensCarrinho();
        setupRecyclerView();

        if (getIntent().hasExtra(EXTRA_PRODUTO_ADICIONAR_CARRINHO)) {
            Produto produtoParaAdicionar = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                produtoParaAdicionar = getIntent().getSerializableExtra(EXTRA_PRODUTO_ADICIONAR_CARRINHO, Produto.class);
            } else {
                produtoParaAdicionar = (Produto) getIntent().getSerializableExtra(EXTRA_PRODUTO_ADICIONAR_CARRINHO);
            }

            if (produtoParaAdicionar != null) {
                adicionarProdutoAoCarrinho(produtoParaAdicionar, 1);
            }
        }

        atualizarVisibilidadeCarrinho();
        calcularEatualizarTotal();

        binding.btnFinalizarPedido.setOnClickListener(v -> finalizarPedido());
    }

    private void setupRecyclerView() {
        binding.recyclerViewItensCarrinho.setLayoutManager(new LinearLayoutManager(this));
        if (itensNoCarrinho == null) {
            itensNoCarrinho = new ArrayList<>();
        }
        carrinhoAdapter = new CarrinhoAdapter(this, itensNoCarrinho, this);
        binding.recyclerViewItensCarrinho.setAdapter(carrinhoAdapter);
    }

    private void adicionarProdutoAoCarrinho(Produto produto, int quantidadeInicial) {
        if (itensNoCarrinho == null) {
            itensNoCarrinho = new ArrayList<>();
        }
        boolean encontrado = false;
        for (CarrinhoItem item : itensNoCarrinho) {
            if (item.getProduto().getId() == produto.getId()) {
                item.incrementarQuantidade();
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            itensNoCarrinho.add(new CarrinhoItem(produto, quantidadeInicial));
        }
        if (carrinhoAdapter != null) {
            carrinhoAdapter.atualizarItens(new ArrayList<>(itensNoCarrinho));
        }
        salvarItensCarrinho();
        atualizarVisibilidadeCarrinho();
        calcularEatualizarTotal();
    }


    private void atualizarVisibilidadeCarrinho() {
        if (itensNoCarrinho == null || itensNoCarrinho.isEmpty()) {
            binding.textViewCarrinhoVazio.setVisibility(View.VISIBLE);
            binding.recyclerViewItensCarrinho.setVisibility(View.GONE);
            binding.btnFinalizarPedido.setEnabled(false);
        } else {
            binding.textViewCarrinhoVazio.setVisibility(View.GONE);
            binding.recyclerViewItensCarrinho.setVisibility(View.VISIBLE);
            binding.btnFinalizarPedido.setEnabled(true);
        }
    }

    private void calcularEatualizarTotal() {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (itensNoCarrinho != null) {
            for (CarrinhoItem item : itensNoCarrinho) {
                if (item.getPrecoTotalItem() != null) { // Verifica se o preço do item não é nulo
                    total = total.add(item.getPrecoTotalItem()); // Usa o método add()
                }
            }
        }
        binding.textViewTotalCarrinho.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", total));
    }

    private void finalizarPedido() {
        if (itensNoCarrinho == null || itensNoCarrinho.isEmpty()) {
            Toast.makeText(this, getString(R.string.carrinho_vazio), Toast.LENGTH_SHORT).show();
            return;
        }
        if (clienteIdLogado == -1) {
            Toast.makeText(this, "Não foi possível identificar o cliente. Faça login.", Toast.LENGTH_LONG).show();
            return;
        }

        binding.progressBarFinalizarPedido.setVisibility(View.VISIBLE);
        binding.btnFinalizarPedido.setEnabled(false);

        StringBuilder descricaoPedido = new StringBuilder();
        BigDecimal valorTotalPedido = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (int i = 0; i < itensNoCarrinho.size(); i++) {
            CarrinhoItem item = itensNoCarrinho.get(i);
            descricaoPedido.append(item.getQuantidade())
                    .append("x ")
                    .append(item.getProduto().getNome());
            if (i < itensNoCarrinho.size() - 1) {
                descricaoPedido.append(", ");
            }
            if (item.getPrecoTotalItem() != null) {
                valorTotalPedido = valorTotalPedido.add(item.getPrecoTotalItem()); // Usa o método add()
            }
        }

        Pedido novoPedido = new Pedido(clienteIdLogado, descricaoPedido.toString(), valorTotalPedido.doubleValue());

        executorService.execute(() -> {
            long pedidoId = pedidoDao.inserir(novoPedido);
            runOnUiThread(() -> {
                binding.progressBarFinalizarPedido.setVisibility(View.GONE);
                binding.btnFinalizarPedido.setEnabled(true);
                if (pedidoId != -1) {
                    Toast.makeText(this, getString(R.string.pedido_realizado_sucesso, pedidoId), Toast.LENGTH_LONG).show();
                    itensNoCarrinho.clear();
                    salvarItensCarrinho();
                    if(carrinhoAdapter != null) carrinhoAdapter.atualizarItens(new ArrayList<>(itensNoCarrinho));
                    atualizarVisibilidadeCarrinho();
                    calcularEatualizarTotal();
                    VisualizarProdutoActivity.setProdutosDesatualizados(true);
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.falha_realizar_pedido), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onItemQuantityChanged() {
        calcularEatualizarTotal();
        salvarItensCarrinho();
    }

    @Override
    public void onItemRemoved(CarrinhoItem item, int position) {
        if (itensNoCarrinho != null && position >= 0 && position < itensNoCarrinho.size()) {
            itensNoCarrinho.remove(position);
            if (carrinhoAdapter != null) {
                carrinhoAdapter.notifyItemRemoved(position);
                carrinhoAdapter.notifyItemRangeChanged(position, itensNoCarrinho.size());
            }
            salvarItensCarrinho();
            atualizarVisibilidadeCarrinho();
            calcularEatualizarTotal();
            Toast.makeText(this, getString(R.string.item_removido_carrinho, item.getProduto().getNome()), Toast.LENGTH_SHORT).show();
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

    private void carregarItensCarrinho() {
        if (clienteIdLogado == -1) {
            itensNoCarrinho = new ArrayList<>();
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences(CARRINHO_PREFS, Context.MODE_PRIVATE);
        // Usa a instância Gson configurada para BigDecimal
        String jsonItens = sharedPreferences.getString(KEY_ITENS_CARRINHO + "_" + clienteIdLogado, null);
        Type type = new TypeToken<ArrayList<CarrinhoItem>>() {}.getType();

        if (jsonItens != null) {
            itensNoCarrinho = gson.fromJson(jsonItens, type);
        }

        if (itensNoCarrinho == null) {
            itensNoCarrinho = new ArrayList<>();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        salvarItensCarrinho();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
