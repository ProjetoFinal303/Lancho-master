package projetofinal.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import projetofinal.adapters.ProdutoAdapter;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Produto;

public class VisualizarProdutoActivity extends AppCompatActivity {

    private ActivityVisualizarProdutoBinding binding;
    private ProdutoDao produtoDao;
    private ProdutoAdapter produtoAdapter;
    private List<Produto> listaDeProdutos;
    private ExecutorService executorService;
    private String userRole;
    private int clienteIdLogado = -1;
    private Gson gson;

    private static boolean produtosDesatualizados = true;
    private static final String TAG = "VisualizarProduto";

    public static void setProdutosDesatualizados(boolean status) {
        Log.i(TAG, "setProdutosDesatualizados chamado com status: " + status);
        produtosDesatualizados = status;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, typeOfSrc, context) -> src == null ? null : context.serialize(src.toPlainString()));
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonDeserializer<BigDecimal>) (json, typeOfT, context) -> json == null ? null : new BigDecimal(json.getAsString()));
        gson = gsonBuilder.create();

        Toolbar toolbar = binding.toolbarVisualizarProdutos;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.visualizar_produtos_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        produtoDao = new ProdutoDao(this);
        executorService = Executors.newSingleThreadExecutor();
        listaDeProdutos = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        setupRecyclerView();

        binding.btnVerCarrinho.setOnClickListener(v -> {
            Intent intent = new Intent(VisualizarProdutoActivity.this, CadastrarPedidoActivity.class);
            startActivity(intent);
        });
        Log.d(TAG, "onCreate finalizado. UserRole: " + userRole);
    }

    private void setupRecyclerView() {
        // ALTERAÇÃO AQUI: De GridLayoutManager para LinearLayoutManager
        binding.recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(this));
        produtoAdapter = new ProdutoAdapter(this, listaDeProdutos, (produto, view) -> {
            if (view.getId() == R.id.btnAdicionarProdutoItem) {
                if ("cliente".equals(userRole)) {
                    adicionarAoCarrinho(produto);
                } else {
                    Toast.makeText(VisualizarProdutoActivity.this, "Apenas clientes podem adicionar itens ao carrinho.", Toast.LENGTH_SHORT).show();
                }
            } else if ("admin".equals(userRole)) {
                Intent intent = new Intent(VisualizarProdutoActivity.this, AtualizarProdutoActivity.class);
                intent.putExtra(AtualizarProdutoActivity.EXTRA_PRODUTO_ID, produto.getId());
                startActivity(intent);
            }
        });
        binding.recyclerViewProdutos.setAdapter(produtoAdapter);
        Log.d(TAG, "RecyclerView configurado com LinearLayoutManager.");
    }

    private void carregarProdutos() {
        Log.i(TAG, "Iniciando carregarProdutos(). Flag desatualizado: " + produtosDesatualizados);
        binding.progressBarProdutos.setVisibility(View.VISIBLE);
        binding.textViewNenhumProduto.setVisibility(View.GONE);
        binding.recyclerViewProdutos.setVisibility(View.GONE);

        executorService.execute(() -> {
            List<Produto> produtosCarregadosDoBanco = produtoDao.listarTodos();
            final List<Produto> finalProdutosCarregados = new ArrayList<>(produtosCarregadosDoBanco != null ? produtosCarregadosDoBanco : new ArrayList<>());
            Log.d(TAG, "Produtos carregados do DAO na background thread: " + finalProdutosCarregados.size());

            runOnUiThread(() -> {
                Log.d(TAG, "Atualizando UI em carregarProdutos(). Produtos carregados: " + finalProdutosCarregados.size());
                listaDeProdutos.clear();
                listaDeProdutos.addAll(finalProdutosCarregados);

                binding.progressBarProdutos.setVisibility(View.GONE);
                if (!listaDeProdutos.isEmpty()) {
                    produtoAdapter.atualizarProdutos(listaDeProdutos);
                    binding.recyclerViewProdutos.setVisibility(View.VISIBLE);
                    Log.d(TAG, "RecyclerView atualizado e visível com " + produtoAdapter.getItemCount() + " produtos.");
                } else {
                    binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Nenhum produto para exibir. TextViewNenhumProduto visível.");
                }
                produtosDesatualizados = false;
                Log.d(TAG, "Flag produtosDesatualizados resetado para false.");
                atualizarContadorCarrinho();
            });
        });
    }

    private void adicionarAoCarrinho(Produto produto) {
        if (clienteIdLogado == -1 && "cliente".equals(userRole)) {
            Toast.makeText(this, "Erro de sessão. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!"cliente".equals(userRole)) {
            Toast.makeText(this, "Apenas clientes podem adicionar ao carrinho.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(CadastrarPedidoActivity.CARRINHO_PREFS, Context.MODE_PRIVATE);
        String jsonItens = sharedPreferences.getString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, null);
        Type type = new TypeToken<ArrayList<CarrinhoItem>>() {}.getType();
        ArrayList<CarrinhoItem> carrinhoAtual = gson.fromJson(jsonItens, type);

        if (carrinhoAtual == null) {
            carrinhoAtual = new ArrayList<>();
        }

        boolean encontrado = false;
        for (CarrinhoItem item : carrinhoAtual) {
            if (item.getProduto().getId() == produto.getId()) {
                item.incrementarQuantidade();
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            carrinhoAtual.add(new CarrinhoItem(produto, 1));
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String novoJsonItens = gson.toJson(carrinhoAtual);
        editor.putString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, novoJsonItens);
        editor.apply();

        Toast.makeText(this, produto.getNome() + " adicionado ao carrinho!", Toast.LENGTH_SHORT).show();
        atualizarContadorCarrinho();
    }

    private void atualizarContadorCarrinho() {
        if (!"cliente".equals(userRole) || clienteIdLogado == -1) {
            binding.btnVerCarrinho.setVisibility(View.GONE);
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(CadastrarPedidoActivity.CARRINHO_PREFS, Context.MODE_PRIVATE);
        String jsonItens = sharedPreferences.getString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, null);
        Type type = new TypeToken<ArrayList<CarrinhoItem>>() {}.getType();
        ArrayList<CarrinhoItem> carrinhoAtual = gson.fromJson(jsonItens, type);

        int totalItens = 0;
        if (carrinhoAtual != null) {
            for (CarrinhoItem item : carrinhoAtual) {
                totalItens += item.getQuantidade();
            }
        }

        if (totalItens > 0) {
            String textoBotao = getString(R.string.ver_carrinho).replace("(%d)", "").trim();
            binding.btnVerCarrinho.setText(String.format(Locale.getDefault(), "%s (%d)", textoBotao, totalItens));
            binding.btnVerCarrinho.setVisibility(View.VISIBLE);
        } else {
            binding.btnVerCarrinho.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("admin".equals(userRole)) {
            getMenuInflater().inflate(R.menu.menu_gerenciar_produtos, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_add_produto) {
            if ("admin".equals(userRole)) {
                startActivity(new Intent(this, CadastrarProdutoActivity.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume chamado. Flag produtosDesatualizados: " + produtosDesatualizados);
        if (produtosDesatualizados) {
            carregarProdutos();
        } else {
            if(produtoAdapter != null && listaDeProdutos != null && produtoAdapter.getItemCount() != listaDeProdutos.size()){
                Log.d(TAG, "onResume: Sincronizando adapter com listaDeProdutos. Adapter: " + produtoAdapter.getItemCount() + ", Lista: " + listaDeProdutos.size());
                produtoAdapter.atualizarProdutos(new ArrayList<>(listaDeProdutos));
            }
            atualizarContadorCarrinho();
        }
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
