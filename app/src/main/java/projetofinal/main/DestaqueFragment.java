package projetofinal.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.FragmentDestaqueBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import projetofinal.models.CarrinhoItem;
import projetofinal.models.Produto;
import projetofinal.dao.ProdutoDao;

public class DestaqueFragment extends Fragment {

    private FragmentDestaqueBinding binding;
    private Produto produtoDestaqueAtual;
    private ProdutoDao produtoDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDestaqueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        produtoDao = new ProdutoDao(getContext());
        carregarDestaqueDoDia();

        // AÇÃO DO BOTÃO DE COMPRAR QUE ESTAVA FALTANDO
        binding.btnComprarDestaque.setOnClickListener(v -> {
            if (produtoDestaqueAtual != null) {
                adicionarAoCarrinho(produtoDestaqueAtual);
            } else {
                Toast.makeText(getContext(), "Destaque indisponível no momento.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarDestaqueDoDia() {
        produtoDao.listarTodos(
                produtos -> {
                    if (getActivity() != null && produtos != null && !produtos.isEmpty()) {
                        getActivity().runOnUiThread(() -> {
                            long daysSinceEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
                            int productIndex = (int) (daysSinceEpoch % produtos.size());
                            produtoDestaqueAtual = produtos.get(productIndex);

                            binding.textViewNomeDestaque.setText(produtoDestaqueAtual.getNome());
                            binding.textViewDescricaoDestaque.setText(produtoDestaqueAtual.getDescricao());
                            Glide.with(this).load(produtoDestaqueAtual.getImageUrl()).placeholder(R.drawable.lanchoxburger).into(binding.imageViewDestaque);
                        });
                    }
                },
                error -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> binding.btnComprarDestaque.setVisibility(View.GONE));
                    }
                }
        );
    }

    private void adicionarAoCarrinho(Produto produto) {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(CadastrarPedidoActivity.CARRINHO_PREFS, Context.MODE_PRIVATE);
        String jsonItens = sharedPreferences.getString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, null);
        Type type = new TypeToken<ArrayList<CarrinhoItem>>() {}.getType();
        List<CarrinhoItem> itensNoCarrinho = new Gson().fromJson(jsonItens, type);
        if (itensNoCarrinho == null) {
            itensNoCarrinho = new ArrayList<>();
        }

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

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String jsonItensAtualizado = new Gson().toJson(itensNoCarrinho);
        editor.putString(CadastrarPedidoActivity.KEY_ITENS_CARRINHO + "_" + clienteIdLogado, jsonItensAtualizado);
        editor.apply();

        Toast.makeText(getContext(), produto.getNome() + " adicionado ao carrinho!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getContext(), CadastrarPedidoActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}