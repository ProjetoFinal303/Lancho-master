package projetofinal.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityVisualizarProdutoBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import projetofinal.adapters.ProdutoAdapter;
import projetofinal.dao.ProdutoDao;
import projetofinal.database.SupabaseFunctionClient;
import projetofinal.database.SupabaseStorageClient;
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Produto;

public class VisualizarProdutoActivity extends BaseActivity implements ProdutoAdapter.OnProdutoInteractionListener {

    private ActivityVisualizarProdutoBinding binding;
    private ProdutoAdapter produtoAdapter;
    private List<Produto> listaDeProdutos = new ArrayList<>();
    private List<CarrinhoItem> itensNoCarrinho = new ArrayList<>();
    private Gson gson;
    private int clienteIdLogado = -1;
    private String userRole;
    private ProdutoDao produtoDao;
    private Uri imagemSelecionadaUri = null;
    private ImageView imgPreviewDialog;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final String TAG = "VisualizarProduto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisualizarProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagemSelecionadaUri = result.getData().getData();
                        if (imgPreviewDialog != null) {
                            Glide.with(this).load(imagemSelecionadaUri).into(imgPreviewDialog);
                            imgPreviewDialog.setVisibility(View.VISIBLE);
                        }
                    }
                });

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, typeOfSrc, context) -> src == null ? null : context.serialize(src.toPlainString()));
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonDeserializer<BigDecimal>) (json, typeOfT, context) -> json == null ? null : new BigDecimal(json.getAsString()));
        gson = gsonBuilder.create();

        produtoDao = new ProdutoDao(this);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");

        configurarToolbar();
        setupRecyclerView();

        binding.btnVerCarrinho.setOnClickListener(v -> startActivity(new Intent(this, CadastrarPedidoActivity.class)));

        if ("admin".equals(userRole)) {
            binding.fabAdicionarProduto.setVisibility(View.VISIBLE);
            binding.fabAdicionarProduto.setOnClickListener(v -> mostrarDialogoAdicionarProduto());
        }

        fetchProductsFromSupabase();
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
            finish();
            return true;
        } else if (itemId == R.id.action_sync_produtos) {
            sincronizarProdutos();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarItensCarrinho();
        atualizarBotaoCarrinho();
    }

    private void setupRecyclerView() {
        binding.recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(this));
        produtoAdapter = new ProdutoAdapter(this, listaDeProdutos, this, userRole);
        binding.recyclerViewProdutos.setAdapter(produtoAdapter);
    }

    @Override
    public void onProdutoClick(Produto produto, View clickedView) {
        adicionarAoCarrinho(produto);
    }

    @Override
    public void onProdutoDeleteClick(Produto produto) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o produto '" + produto.getNome() + "' do Stripe? Esta ação não pode ser desfeita.")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> deletarProduto(produto))
                .setNegativeButton("Não", null)
                .show();
    }

    private void configurarToolbar() {
        Toolbar toolbar = binding.toolbarVisualizarProdutos;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if ("admin".equals(userRole)) {
                getSupportActionBar().setTitle("Gerenciar Produtos");
            } else {
                getSupportActionBar().setTitle("Cardápio");
            }
        }
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
                        binding.textViewNenhumProduto.setText(R.string.nenhum_produto_cadastrado);
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao buscar produtos do Supabase: ", error);
                    binding.recyclerViewProdutos.setVisibility(View.GONE);
                    binding.textViewNenhumProduto.setText("Erro ao carregar o cardápio.");
                    binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarProdutos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.recyclerViewProdutos.setVisibility(View.GONE);
            binding.textViewNenhumProduto.setVisibility(View.GONE);
        }
    }

    private void sincronizarProdutos() {
        setLoading(true);
        Toast.makeText(this, "Sincronizando...", Toast.LENGTH_SHORT).show();
        SupabaseFunctionClient.invoke("sync-stripe-products", new JSONObject(),
                response -> runOnUiThread(() -> {
                    Toast.makeText(this, "Sincronização concluída!", Toast.LENGTH_SHORT).show();
                    fetchProductsFromSupabase();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao sincronizar: ", error);
                    Toast.makeText(this, "Falha na sincronização.", Toast.LENGTH_SHORT).show();
                    fetchProductsFromSupabase();
                })
        );
    }

    private void deletarProduto(Produto produto) {
        setLoading(true);
        try {
            JSONObject payload = new JSONObject();
            payload.put("stripe_price_id", produto.getId());

            SupabaseFunctionClient.invoke("delete-stripe-product", payload,
                    response -> runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(this, "Produto excluído do Stripe. Sincronize para atualizar a lista.", Toast.LENGTH_LONG).show();
                    }),
                    error -> runOnUiThread(() -> {
                        setLoading(false);
                        Log.e(TAG, "Erro ao deletar: ", error);
                        Toast.makeText(this, "Falha ao excluir produto.", Toast.LENGTH_SHORT).show();
                    })
            );
        } catch (JSONException e) {
            setLoading(false);
            Toast.makeText(this, "Erro ao preparar dados para exclusão.", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoAdicionarProduto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Novo Produto");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_produto, (ViewGroup) binding.getRoot(), false);
        final EditText inputNome = viewInflated.findViewById(R.id.edtNomeProdutoDialog);
        final EditText inputDesc = viewInflated.findViewById(R.id.edtDescricaoProdutoDialog);
        final EditText inputPreco = viewInflated.findViewById(R.id.edtPrecoProdutoDialog);
        final Button btnEscolherImagem = viewInflated.findViewById(R.id.btnEscolherImagemDialog);
        imgPreviewDialog = viewInflated.findViewById(R.id.imgPreviewDialog);

        imagemSelecionadaUri = null;
        imgPreviewDialog.setVisibility(View.GONE);

        btnEscolherImagem.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        builder.setView(viewInflated);

        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            try {
                String nome = inputNome.getText().toString().trim();
                String desc = inputDesc.getText().toString().trim();
                double preco = Double.parseDouble(inputPreco.getText().toString().trim());

                if(nome.isEmpty() || preco <= 0) {
                    Toast.makeText(this, "Nome e preço são obrigatórios.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imagemSelecionadaUri != null) {
                    Toast.makeText(this, "Fazendo upload da imagem...", Toast.LENGTH_SHORT).show();
                    setLoading(true);
                    SupabaseStorageClient.uploadFile(this, imagemSelecionadaUri,
                            publicUrl -> runOnUiThread(() -> adicionarProdutoAoStripe(nome, desc, preco, publicUrl)),
                            error -> runOnUiThread(() -> {
                                Toast.makeText(this, "Falha no upload da imagem.", Toast.LENGTH_SHORT).show();
                                setLoading(false);
                            })
                    );
                } else {
                    adicionarProdutoAoStripe(nome, desc, preco, null);
                }

            } catch (Exception e) {
                Toast.makeText(this, "Dados inválidos.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void adicionarProdutoAoStripe(String nome, String desc, double preco, String imageUrl) {
        setLoading(true);
        try {
            JSONObject payload = new JSONObject();
            payload.put("nome", nome);
            payload.put("descricao", desc);
            payload.put("preco", preco);
            payload.put("imageUrl", imageUrl);

            adicionarProduto(payload);

        } catch (JSONException e) {
            setLoading(false);
            Toast.makeText(this, "Erro ao criar dados para envio.", Toast.LENGTH_SHORT).show();
        }
    }

    private void adicionarProduto(JSONObject payload) {
        SupabaseFunctionClient.invoke("add-stripe-product", payload,
                response -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Produto adicionado ao Stripe! Sincronize para ver na lista.", Toast.LENGTH_LONG).show();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao adicionar: ", error);
                    Toast.makeText(this, "Falha ao adicionar produto.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void adicionarAoCarrinho(Produto produto) {
        if(itensNoCarrinho == null) itensNoCarrinho = new ArrayList<>();
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

        int totalItens = 0;
        if (itensNoCarrinho != null) {
            totalItens = itensNoCarrinho.stream().filter(Objects::nonNull).mapToInt(CarrinhoItem::getQuantidade).sum();
        }

        if (totalItens > 0) {
            binding.btnVerCarrinho.setVisibility(View.VISIBLE);
            binding.btnVerCarrinho.setText(String.format(Locale.getDefault(), "Ver Carrinho (%d)", totalItens));
        } else {
            binding.btnVerCarrinho.setVisibility(View.GONE);
        }
    }
}