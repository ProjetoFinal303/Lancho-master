package projetofinal.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import projetofinal.adapters.CarrinhoAdapter;
import projetofinal.dao.ClienteDao;
import projetofinal.models.CarrinhoItem;
import projetofinal.models.Cliente;

public class CadastrarPedidoActivity extends BaseActivity implements CarrinhoAdapter.OnCarrinhoInteractionListener {

    private ActivityCadastrarPedidoBinding binding;
    private CarrinhoAdapter carrinhoAdapter;
    private List<CarrinhoItem> itensNoCarrinho = new ArrayList<>();
    private int clienteIdLogado = -1;
    private Gson gson;
    private Cliente clienteLogado;
    private ClienteDao clienteDao;
    public static final String CARRINHO_PREFS = "CarrinhoPrefs";
    public static final String KEY_ITENS_CARRINHO = "ItensCarrinho";
    private static final String TAG = "CadastrarPedido";

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

        clienteDao = new ClienteDao(this);
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        clienteIdLogado = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (clienteIdLogado == -1) {
            Toast.makeText(this, "Erro: Sessão de cliente inválida. Faça login.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        carregarDadosCliente();
        carregarItensCarrinho();
        setupRecyclerView();
        atualizarVisibilidadeCarrinho();
        calcularEAtualizarTotal();

        binding.btnFinalizarPedido.setOnClickListener(v -> iniciarCheckoutStripe());
    }

    private void carregarDadosCliente() {
        if (clienteIdLogado != -1) {
            clienteDao.buscarPorId(clienteIdLogado,
                    cliente -> {
                        if (cliente != null) this.clienteLogado = cliente;
                        else runOnUiThread(() -> Toast.makeText(this, "Erro ao carregar seus dados.", Toast.LENGTH_LONG).show());
                    },
                    error -> runOnUiThread(() -> Toast.makeText(this, "Erro de conexão.", Toast.LENGTH_LONG).show())
            );
        }
    }

    private void iniciarCheckoutStripe() {
        if (itensNoCarrinho.isEmpty()) {
            Toast.makeText(this, "Seu carrinho está vazio.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (clienteLogado == null || clienteLogado.getEmail() == null) {
            Toast.makeText(this, "Aguarde, carregando seus dados...", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        String functionUrl = "https://ygsziltorjcgpjbmlptr.supabase.co/functions/v1/create-mobile-checkout";
        String anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlnc3ppbHRvcmpjZ3BqYm1scHRyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgyOTUzNTQsImV4cCI6MjA2Mzg3MTM1NH0.3J19gnI_qwM3nWolVdvCcNNusC3YlOTvZEjOwM6z2PU";

        List<CheckoutItemPayload> itemsPayload = itensNoCarrinho.stream()
                .map(item -> new CheckoutItemPayload(item.getPriceId(), item.getQuantidade()))
                .collect(Collectors.toList());

        String jsonPayload = gson.toJson(new CheckoutPayload(itemsPayload, clienteLogado.getEmail(), clienteIdLogado));

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(functionUrl)
                .header("Authorization", "Bearer " + anonKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(CadastrarPedidoActivity.this, "Falha na comunicação com o servidor.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (response) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String checkoutUrl = jsonResponse.getString("checkoutUrl");

                        runOnUiThread(() -> {
                            itensNoCarrinho.clear();
                            salvarItensCarrinho();
                            Toast.makeText(CadastrarPedidoActivity.this, "Redirecionando para o pagamento...", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)));
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(CadastrarPedidoActivity.this, "Erro ao criar sessão de pagamento.", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(CadastrarPedidoActivity.this, "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerViewItensCarrinho.setLayoutManager(new LinearLayoutManager(this));
        carrinhoAdapter = new CarrinhoAdapter(this, itensNoCarrinho, this);
        binding.recyclerViewItensCarrinho.setAdapter(carrinhoAdapter);
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
        BigDecimal total = itensNoCarrinho.stream()
                .map(CarrinhoItem::getPrecoTotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
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

    private static class CheckoutItemPayload {
        final String priceId;
        final int quantity;
        CheckoutItemPayload(String priceId, int quantity) {
            this.priceId = priceId;
            this.quantity = quantity;
        }
    }
    private static class CheckoutPayload {
        final List<CheckoutItemPayload> cartItems;
        final String customerEmail;
        final int clienteId;
        CheckoutPayload(List<CheckoutItemPayload> cartItems, String customerEmail, int clienteId) {
            this.cartItems = cartItems;
            this.customerEmail = customerEmail;
            this.clienteId = clienteId;
        }
    }
}