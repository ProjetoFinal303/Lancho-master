package projetofinal.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.projetofinal.databinding.ActivityAvaliarPedidoBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import projetofinal.adapters.AvaliacaoAdapter;
import projetofinal.dao.AvaliacaoDao;
import projetofinal.dao.ProdutoDao;
import projetofinal.models.Avaliacao;
import projetofinal.models.Produto;

public class AvaliarPedidoActivity extends BaseActivity {

    private ActivityAvaliarPedidoBinding binding;
    private AvaliacaoAdapter avaliacaoAdapter;
    private ProdutoDao produtoDao;
    private AvaliacaoDao avaliacaoDao;
    private List<Produto> produtosParaAvaliar = new ArrayList<>();
    private static final String TAG = "AvaliarPedidoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAvaliarPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        produtoDao = new ProdutoDao(this);
        avaliacaoDao = new AvaliacaoDao(this);

        setupToolbar();
        setupRecyclerView();

        String descricaoPedido = getIntent().getStringExtra("descricaoPedido");
        if (descricaoPedido != null) {
            carregarProdutosDaDescricao(descricaoPedido);
        } else {
            Toast.makeText(this, "Erro: Pedido não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnEnviarAvaliacao.setOnClickListener(v -> enviarAvaliacoes());
    }

    private void setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        avaliacaoAdapter = new AvaliacaoAdapter(this, produtosParaAvaliar);
        binding.recyclerViewItensAvaliacao.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewItensAvaliacao.setAdapter(avaliacaoAdapter);
    }

    private void carregarProdutosDaDescricao(String descricao) {
        setLoading(true);
        // Lógica simples para extrair nomes de produtos da descrição.
        // Em um app real, o ideal seria passar uma lista de IDs de produto.
        String[] itens = descricao.split("\\n");
        List<String> nomesProdutos = new ArrayList<>();
        for (String item : itens) {
            // Remove a quantidade (ex: "1x ") para pegar só o nome
            String nomeLimpo = item.replaceAll("^\\d+x\\s", "").trim();
            nomesProdutos.add(nomeLimpo);
        }

        // Busca todos os produtos e filtra pelos nomes encontrados
        produtoDao.listarTodos(
                todosOsProdutos -> runOnUiThread(() -> {
                    for (String nome : nomesProdutos) {
                        for (Produto p : todosOsProdutos) {
                            if (p.getNome().equalsIgnoreCase(nome)) {
                                produtosParaAvaliar.add(p);
                                break;
                            }
                        }
                    }
                    avaliacaoAdapter.notifyDataSetChanged();
                    setLoading(false);
                }),
                error -> runOnUiThread(() -> {
                    Log.e(TAG, "Erro ao carregar produtos", error);
                    Toast.makeText(this, "Não foi possível carregar os produtos do pedido.", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                })
        );
    }

    private void enviarAvaliacoes() {
        setLoading(true);
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        int clienteId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (clienteId == -1) {
            Toast.makeText(this, "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        List<Avaliacao> avaliacoesParaEnviar = avaliacaoAdapter.getAvaliacoes();
        if (avaliacoesParaEnviar.isEmpty()) {
            Toast.makeText(this, "Por favor, dê uma nota de pelo menos uma estrela para avaliar.", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        // Precisamos enviar cada avaliação individualmente
        AtomicInteger successCount = new AtomicInteger(0);
        int totalAvaliacoes = avaliacoesParaEnviar.size();

        for (Avaliacao avaliacao : avaliacoesParaEnviar) {
            avaliacao.setClienteId(clienteId); // Define o ID do cliente na avaliação
            avaliacaoDao.inserir(avaliacao,
                    () -> { // onSuccess
                        if (successCount.incrementAndGet() == totalAvaliacoes) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Obrigado pelo seu feedback!", Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }
                    },
                    error -> { // onError
                        Log.e(TAG, "Erro ao enviar avaliação para produto ID " + avaliacao.getProdutoId() + ": " + error);
                        if (successCount.incrementAndGet() == totalAvaliacoes) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Algumas avaliações não puderam ser enviadas.", Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }
                    }
            );
        }
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarAvaliacao.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnEnviarAvaliacao.setEnabled(!isLoading);
        binding.recyclerViewItensAvaliacao.setAlpha(isLoading ? 0.5f : 1.0f);
    }
}