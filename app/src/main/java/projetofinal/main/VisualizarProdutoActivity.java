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
import android.widget.ImageView; // <<<--- CORREÇÃO AQUI
import android.widget.LinearLayout; // <<<--- E AQUI
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
import com.google.android.material.textfield.TextInputEditText;
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
import projetofinal.dao.EstoqueDao;
import projetofinal.dao.ProdutoDao;
import projetofinal.database.SupabaseFunctionClient;
import projetofinal.database.SupabaseStorageClient;
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Estoque;
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
    private EstoqueDao estoqueDao;
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
        estoqueDao = new EstoqueDao(this);

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        userRole = prefs.getString(LoginActivity.KEY_USER_ROLE, "");
        try {
            clienteIdLogado = Integer.parseInt(prefs.getString(LoginActivity.KEY_USER_ID, "-1"));
        } catch (Exception e) {
            clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        }

        configurarToolbar();
        setupRecyclerView();

        binding.btnVerCarrinho.setOnClickListener(v -> startActivity(new Intent(this, CadastrarPedidoActivity.class)));

        if ("admin".equals(userRole)) {
            binding.fabAdicionarProduto.setVisibility(View.VISIBLE);
            binding.fabAdicionarProduto.setOnClickListener(v -> mostrarDialogoAdicionarProduto());
        } else {
            binding.fabAdicionarProduto.setVisibility(View.GONE);
        }

        fetchProductsFromSupabase();
    }

    private void mostrarDialogoEditarProduto(Produto produto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Produto e Estoque");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_produto, (ViewGroup) binding.getRoot(), false);
        final TextInputEditText inputNome = viewInflated.findViewById(R.id.edtNomeProdutoDialog);
        final TextInputEditText inputDesc = viewInflated.findViewById(R.id.edtDescricaoProdutoDialog);
        final TextInputEditText inputPreco = viewInflated.findViewById(R.id.edtPrecoProdutoDialog);
        final TextInputEditText inputEstoque = viewInflated.findViewById(R.id.edtEstoqueProdutoDialog);
        final Button btnEscolherImagem = viewInflated.findViewById(R.id.btnEscolherImagemDialog);
        imgPreviewDialog = viewInflated.findViewById(R.id.imgPreviewDialog);

        inputNome.setText(produto.getNome());
        inputDesc.setText(produto.getDescricao());
        inputPreco.setText(produto.getPreco().toPlainString());
        inputEstoque.setText(String.valueOf(produto.getQuantidadeEstoque()));

        if (produto.getImageUrl() != null && !produto.getImageUrl().isEmpty()) {
            Glide.with(this).load(produto.getImageUrl()).into(imgPreviewDialog);
            imgPreviewDialog.setVisibility(View.VISIBLE);
        }

        imagemSelecionadaUri = null;
        btnEscolherImagem.setText("Alterar Imagem (Opcional)");
        btnEscolherImagem.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        builder.setView(viewInflated);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = inputNome.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String precoStr = inputPreco.getText().toString().trim();
            String estoqueStr = inputEstoque.getText().toString().trim();

            if (nome.isEmpty() || precoStr.isEmpty() || estoqueStr.isEmpty()) {
                Toast.makeText(this, "Nome, preço e estoque são obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                BigDecimal preco = new BigDecimal(precoStr);
                int estoque = Integer.parseInt(estoqueStr);
                salvarAtualizacoesProdutoEEstoque(produto, nome, desc, preco, estoque);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Preço ou estoque inválido.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void salvarAtualizacoesProdutoEEstoque(Produto produtoOriginal, String novoNome, String novaDesc, BigDecimal novoPreco, int novaQuantidade) {
        setLoading(true);

        produtoOriginal.setNome(novoNome);
        produtoOriginal.setDescricao(novaDesc);
        produtoOriginal.setPreco(novoPreco);

        if (imagemSelecionadaUri != null) {
            SupabaseStorageClient.uploadFile(this, imagemSelecionadaUri,
                    publicUrl -> runOnUiThread(() -> {
                        produtoOriginal.setImageUrl(publicUrl);
                        atualizarProdutoEEstoqueNoBanco(produtoOriginal, novaQuantidade);
                    }),
                    error -> runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(this, "Erro no upload da imagem: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    })
            );
        } else {
            atualizarProdutoEEstoqueNoBanco(produtoOriginal, novaQuantidade);
        }
    }

    private void atualizarProdutoEEstoqueNoBanco(Produto produtoAtualizado, int novaQuantidade) {
        produtoDao.atualizar(produtoAtualizado,
                responseProduto -> runOnUiThread(() -> {
                    Estoque estoque = new Estoque();
                    estoque.setProdutoId(produtoAtualizado.getId());
                    estoque.setQuantidade(novaQuantidade);

                    estoqueDao.inserirOuAtualizar(estoque,
                            responseEstoque -> runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(this, "Produto e estoque atualizados!", Toast.LENGTH_SHORT).show();
                                fetchProductsFromSupabase();
                            }),
                            errorEstoque -> runOnUiThread(() -> {
                                setLoading(false);
                                Log.e(TAG, "Erro ao atualizar estoque: ", errorEstoque);
                                Toast.makeText(this, "Erro ao salvar estoque: " + errorEstoque.getMessage(), Toast.LENGTH_LONG).show();
                            })
                    );
                }),
                errorProduto -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro ao atualizar produto: ", errorProduto);
                    Toast.makeText(this, "Erro ao salvar produto: " + errorProduto.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void mostrarDialogoAdicionarProduto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Novo Produto");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_produto, (ViewGroup) binding.getRoot(), false);
        final TextInputEditText inputNome = viewInflated.findViewById(R.id.edtNomeProdutoDialog);
        final TextInputEditText inputDesc = viewInflated.findViewById(R.id.edtDescricaoProdutoDialog);
        final TextInputEditText inputPreco = viewInflated.findViewById(R.id.edtPrecoProdutoDialog);
        final TextInputEditText inputEstoque = viewInflated.findViewById(R.id.edtEstoqueProdutoDialog);
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
                int estoque = Integer.parseInt(inputEstoque.getText().toString().trim());

                if (nome.isEmpty() || preco <= 0) {
                    Toast.makeText(this, "Nome e preço são obrigatórios.", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject payload = new JSONObject();
                payload.put("nome", nome);
                payload.put("descricao", desc);
                payload.put("preco", preco);
                payload.put("quantidadeEstoque", estoque);

                if (imagemSelecionadaUri != null) {
                    setLoading(true);
                    Toast.makeText(this, "Fazendo upload da imagem...", Toast.LENGTH_SHORT).show();
                    SupabaseStorageClient.uploadFile(this, imagemSelecionadaUri,
                            publicUrl -> runOnUiThread(() -> {
                                try {
                                    payload.put("imageUrl", publicUrl);
                                    adicionarProduto(payload);
                                } catch (JSONException e) {
                                    setLoading(false);
                                    Toast.makeText(this, "Erro ao preparar dados da imagem.", Toast.LENGTH_SHORT).show();
                                }
                            }),
                            error -> runOnUiThread(() -> {
                                setLoading(false);
                                Toast.makeText(this, "Falha no upload da imagem: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            })
                    );
                } else {
                    payload.put("imageUrl", (Object) null);
                    adicionarProduto(payload);
                }

            } catch (Exception e) {
                Toast.makeText(this, "Dados inválidos.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro no botão Adicionar", e);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void adicionarProduto(JSONObject payload) {
        setLoading(true);
        SupabaseFunctionClient.invoke("add-stripe-product", payload,
                response -> runOnUiThread(() -> {
                    Toast.makeText(this, "Produto adicionado com sucesso!", Toast.LENGTH_LONG).show();
                    fetchProductsFromSupabase();
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Log.e(TAG, "Erro Supabase ao adicionar: ", error);
                    Toast.makeText(this, "Falha ao adicionar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void deletarProduto(Produto produto) {
        setLoading(true);
        try {
            JSONObject payload = new JSONObject();
            payload.put("stripe_price_id", produto.getStripePriceId());

            SupabaseFunctionClient.invoke("delete-stripe-product", payload,
                    response -> runOnUiThread(() -> {
                        Toast.makeText(this, "Produto '" + produto.getNome() + "' excluído.", Toast.LENGTH_LONG).show();
                        fetchProductsFromSupabase();
                    }),
                    error -> runOnUiThread(() -> {
                        setLoading(false);
                        Log.e(TAG, "Erro Supabase ao deletar: ", error);
                        Toast.makeText(this, "Falha ao excluir: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    })
            );
        } catch (JSONException e) {
            setLoading(false);
            Toast.makeText(this, "Erro interno ao preparar dados para exclusão.", Toast.LENGTH_SHORT).show();
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
        fetchProductsFromSupabase();
    }

    private void setupRecyclerView() {
        binding.recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(this));
        produtoAdapter = new ProdutoAdapter(this, listaDeProdutos, this, userRole);
        binding.recyclerViewProdutos.setAdapter(produtoAdapter);
    }

    @Override
    public void onProdutoClick(Produto produto, View clickedView) {
        if ("admin".equals(userRole)) {
            mostrarDialogoEditarProduto(produto);
        } else {
            adicionarAoCarrinho(produto);
        }
    }

    @Override
    public void onProdutoDeleteClick(Produto produto) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o produto '" + produto.getNome() + "'?")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> deletarProduto(produto))
                .setNegativeButton("Não", null)
                .show();
    }

    private void configurarToolbar() {
        Toolbar toolbar = binding.toolbarVisualizarProdutos;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("admin".equals(userRole) ? "Gerenciar Produtos" : "Cardápio");
        }
    }

    private void fetchProductsFromSupabase() {
        setLoading(true);
        produtoDao.listarTodosComEstoque(
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
                    Log.e(TAG, "Erro ao buscar produtos: ", error);
                    binding.recyclerViewProdutos.setVisibility(View.GONE);
                    binding.textViewNenhumProduto.setText("Erro ao carregar o cardápio.");
                    binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarProdutos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if(isLoading) {
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
                    Toast.makeText(this, "Falha na sincronização: " + error.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
    }

    private void adicionarAoCarrinho(Produto produto) {
        if (itensNoCarrinho == null) itensNoCarrinho = new ArrayList<>();
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
        itensNoCarrinho = (jsonItens != null) ? gson.fromJson(jsonItens, type) : new ArrayList<>();
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
            for(CarrinhoItem item : itensNoCarrinho) {
                if(item != null) totalItens += item.getQuantidade();
            }
        }

        if (totalItens > 0) {
            binding.btnVerCarrinho.setVisibility(View.VISIBLE);
            binding.btnVerCarrinho.setText(String.format(Locale.getDefault(), "Ver Carrinho (%d)", totalItens));
        } else {
            binding.btnVerCarrinho.setVisibility(View.GONE);
        }
    }
}