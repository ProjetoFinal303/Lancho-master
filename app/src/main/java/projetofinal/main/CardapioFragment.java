package projetofinal.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.FragmentCardapioBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import projetofinal.adapters.ProdutoAdapter;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Produto;

public class CardapioFragment extends Fragment implements ProdutoAdapter.OnProdutoInteractionListener {

    private FragmentCardapioBinding binding;
    private ProdutoAdapter produtoAdapter;
    private List<Produto> listaDeProdutos = new ArrayList<>();
    private List<CarrinhoItem> itensNoCarrinho = new ArrayList<>();
    private Gson gson;
    private int clienteIdLogado = -1;
    private ProdutoDao produtoDao;
    private static final String TAG = "CardapioFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCardapioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonSerializer<BigDecimal>) (src, typeOfSrc, context) -> src == null ? null : context.serialize(src.toPlainString()));
        gsonBuilder.registerTypeAdapter(BigDecimal.class, (JsonDeserializer<BigDecimal>) (json, typeOfT, context) -> json == null ? null : new BigDecimal(json.getAsString()));
        gson = gsonBuilder.create();

        produtoDao = new ProdutoDao(getContext());
        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        setupRecyclerView();
        fetchProductsFromSupabase();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarItensCarrinho();
    }

    private void setupRecyclerView() {
        binding.recyclerViewProdutos.setLayoutManager(new LinearLayoutManager(getContext()));
        produtoAdapter = new ProdutoAdapter(getContext(), listaDeProdutos, this, "cliente");
        binding.recyclerViewProdutos.setAdapter(produtoAdapter);
    }

    private void fetchProductsFromSupabase() {
        setLoading(true);
        produtoDao.listarTodos(
                produtosRecebidos -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            if (produtosRecebidos != null && !produtosRecebidos.isEmpty()) {
                                produtoAdapter.atualizarProdutos(produtosRecebidos);
                                binding.recyclerViewProdutos.setVisibility(View.VISIBLE);
                                binding.textViewNenhumProduto.setVisibility(View.GONE);
                            } else {
                                binding.recyclerViewProdutos.setVisibility(View.GONE);
                                binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                },
                error -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setLoading(false);
                            Log.e(TAG, "Erro ao buscar produtos: ", error);
                            binding.textViewNenhumProduto.setText("Erro ao carregar o cardápio.");
                            binding.textViewNenhumProduto.setVisibility(View.VISIBLE);
                        });
                    }
                }
        );
    }

    private void setLoading(boolean isLoading) {
        if (binding != null) {
            binding.progressBarProdutos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.recyclerViewProdutos.setVisibility(View.GONE);
                binding.textViewNenhumProduto.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onProdutoClick(Produto produto, View clickedView) {
        adicionarAoCarrinho(produto);
    }

    @Override
    public void onProdutoDeleteClick(Produto produto) {
        // Não aplicável para clientes
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

        Toast.makeText(getContext(), produto.getNome() + " adicionado!", Toast.LENGTH_SHORT).show();
        salvarItensCarrinho();

        if (getActivity() instanceof MainClienteActivity) {
            ((MainClienteActivity) getActivity()).atualizarBotaoCarrinho();
        }
    }

    private void carregarItensCarrinho() {
        if (clienteIdLogado == -1 || getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(CadastrarPedidoActivity.CARRINHO_PREFS, Context.MODE_PRIVATE);
        String jsonItens = sharedPreferences.getString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, null);
        Type type = new TypeToken<ArrayList<CarrinhoItem>>() {}.getType();

        if (jsonItens != null) {
            itensNoCarrinho = gson.fromJson(jsonItens, type);
        } else {
            itensNoCarrinho = new ArrayList<>();
        }
    }

    private void salvarItensCarrinho() {
        if (clienteIdLogado == -1 || getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(CadastrarPedidoActivity.CARRINHO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String jsonItens = gson.toJson(itensNoCarrinho);
        // ---> CORREÇÃO DO TYPO AQUI <---
        editor.putString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, jsonItens);
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}