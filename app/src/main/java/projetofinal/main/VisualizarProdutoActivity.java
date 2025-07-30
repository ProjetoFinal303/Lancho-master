package projetofinal.main;

import android.content.Context;
import android.content.Intent;
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
import com.example.projetofinal.databinding.ActivityVisualizarProdutoBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import projetofinal.adapters.ProdutoAdapter;
import projetofinal.dao.ProdutoDao; // Importar o novo DAO
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Produto;

public class VisualizarProdutoActivity extends BaseActivity {

    private ActivityVisualizarProdutoBinding binding;
    private ProdutoAdapter produtoAdapter;
    private List<Produto> listaDeProdutos = new ArrayList<>();
    private List<CarrinhoItem> itensNoCarrinho = new ArrayList<>();
    private Gson gson;
    private int clienteIdLogado = -1;
    private String userRole;
    private ProdutoDao produtoDao; // Adicionar o DAO
    private static final String TAG = "VisualizarProduto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, typeOfSrc, context) -> src == null ? null : context.serialize(src.toPlainString()));
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonDeserializer<BigDecimal>) (json, typeOfT, context) -> json == null ? null : new BigDecimal(json.getAsString()));
        gson = gsonBuilder.create();

        produtoDao = new ProdutoDao(this); // Instanciar o DAO

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");

        configurarToolbar();
        setupRecyclerView();

        binding.btnVerCarrinho.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastrarPedidoActivity.class));
        });

        // MODIFICADO: Busca produtos do banco de dados
        fetchProductsFromSupabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarItensCarrinho();
        atualizarBotaoCarrinho();
    }

    private void configurarToolbar() {
        Toolbar toolbar = binding.toolbarVisualizarProdutos;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if ("admin".equals(userRole)) {
                getSupportActionBar().setTitle("Produtos (Sincronizados)");
            } else {
                getSupportActionBar().setTitle("Cardápio");
            }
        }
    }

    private void setupRecyclerView() {
        binding.recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(this));
        produtoAdapter = new ProdutoAdapter(this, listaDeProdutos, (produto, clickedView) -> {
            adicionarAoCarrinho(produto);
        }, userRole);
        binding.recyclerViewProdutos.setAdapter(produtoAdapter);
    }

    private void fetchProductsFromSupabase() {
        setLoading(true);
        produtoDao.listarTodos(
                produtosRecebidos -> runOnUiThread(() -> {
                    setLoading(false);
                    if (produtosRecebidos != null && !produtosRecebidos.isEmpty()) {
                        produtoAdapter.atualizarProdutos(produtosRecebidos);
                        binding.recyclerViewProdutos.setVisibility(View.VISIBLE);
                        binding.textViewNenhumProduto.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewProdutos.setVisibility(View.GONE);
                        binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao buscar produtos do Supabase: ", error);
                    binding.textViewNenhumProduto.setText("Erro ao carregar o cardápio.");
                    binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                })
        );
    }

    private void adicionarAoCarrinho(Produto produto) {
        CarrinhoItem itemExistente = null;
        for (CarrinhoItem item : itensNoCarrinho) {
            if (Objects.equals(item.getProduto().getId(), produto.getId())) {
                itemExistente = item;
                break;
            }
        }

        if (itemExistente != null) {
            itemExistente.incrementarQuantidade();
        } else {
            itensNoCarrinho.add(new CarrinhoItem(produto, 1));
        }

        Toast.makeText(this, produto.getNome() + " adicionado!", Toast.LENGTH_SHORT).show();
        salvarItensCarrinho();
        atualizarBotaoCarrinho();
    }

    private void carregarItensCarrinho() {
        if (clienteIdLogado == -1) return;
        SharedPreferences sharedPreferences = getSharedPreferences(CadastrarPedidoActivity.CARRINHO_PREFS, Context.MODE_PRIVATE);
        String jsonItens = sharedPreferences.getString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, null);
        Type type = new TypeToken<ArrayList<CarrinhoItem>>() {}.getType();

        if (jsonItens != null) {
            itensNoCarrinho = gson.fromJson(jsonItens, type);
        } else {
            itensNoCarrinho = new ArrayList<>();
        }
    }

    private void salvarItensCarrinho() {
        if (clienteIdLogado == -1) return;
        SharedPreferences sharedPreferences = getSharedPreferences(CadastrarPedidoActivity.CARRINHO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String jsonItens = gson.toJson(itensNoCarrinho);
        editor.putString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, jsonItens);
        editor.apply();
    }

    private void atualizarBotaoCarrinho() {
        if ("admin".equals(userRole)) {
            binding.btnVerCarrinho.setVisibility(View.GONE);
            return;
        }

        int totalItens = itensNoCarrinho.stream().mapToInt(CarrinhoItem::getQuantidade).sum();

        if (totalItens > 0) {
            binding.btnVerCarrinho.setVisibility(View.VISIBLE);
            binding.btnVerCarrinho.setText(String.format(Locale.getDefault(), "Ver Carrinho (%d)", totalItens));
        } else {
            binding.btnVerCarrinho.setVisibility(View.GONE);
        }
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarProdutos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.recyclerViewProdutos.setVisibility(View.GONE);
            binding.textViewNenhumProduto.setVisibility(View.GONE);
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