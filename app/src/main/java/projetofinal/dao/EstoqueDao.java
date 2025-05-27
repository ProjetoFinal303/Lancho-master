package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Estoque;
// projetofinal.models.Produto; // Não é estritamente necessário importar Produto aqui se não for construir objetos Produto

public class EstoqueDao {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "EstoqueDao";

    public EstoqueDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Insere uma nova entrada de estoque ou atualiza a quantidade se o produto já existir no estoque.
     * A coluna 'id_produto' na tabela Estoque é UNIQUE.
     *
     * @param estoque O objeto Estoque a ser inserido ou atualizado.
     * @return O ID da linha do estoque inserido/atualizado, ou -1 em caso de erro.
     */
    public long inserirOuAtualizar(Estoque estoque) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long idRetornado = -1;

        // Verifica se já existe um estoque para este produto_id
        Estoque estoqueExistente = buscarPorProdutoId(estoque.getProdutoId());

        try {
            ContentValues values = new ContentValues();
            values.put("id_produto", estoque.getProdutoId());
            values.put("quantidade", estoque.getQuantidade());

            if (estoqueExistente != null) {
                // Atualiza a quantidade do estoque existente
                // O ID do Estoque (chave primária da tabela Estoque) é usado para o update
                int linhasAfetadas = db.update("Estoque", values, "id = ?", new String[]{String.valueOf(estoqueExistente.getId())});
                if (linhasAfetadas > 0) {
                    idRetornado = estoqueExistente.getId(); // Retorna o ID do estoque atualizado
                    Log.d(TAG, "Estoque atualizado para produto ID: " + estoque.getProdutoId() + ", Nova Quantidade: " + estoque.getQuantidade());
                } else {
                    Log.e(TAG, "Falha ao atualizar estoque para produto ID: " + estoque.getProdutoId());
                }
            } else {
                // Insere uma nova entrada de estoque, pois não existe para este produto_id
                // O ID do Estoque (chave primária) será autoincrementado
                idRetornado = db.insert("Estoque", null, values);
                if (idRetornado != -1) {
                    Log.d(TAG, "Novo estoque inserido para produto ID: " + estoque.getProdutoId() + ", Quantidade: " + estoque.getQuantidade());
                } else {
                    Log.e(TAG, "Falha ao inserir novo estoque para produto ID: " + estoque.getProdutoId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inserir ou atualizar estoque: " + e.getMessage(), e);
        }
        // Não feche o db aqui se dbHelper for singleton gerenciando a conexão
        return idRetornado;
    }


    /**
     * Busca uma entrada de estoque pelo ID do produto.
     *
     * @param produtoId O ID do produto.
     * @return O objeto Estoque se encontrado, caso contrário null.
     */
    public Estoque buscarPorProdutoId(int produtoId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Estoque estoque = null;
        Cursor cursor = null;
        try {
            cursor = db.query("Estoque",
                    new String[]{"id", "id_produto", "quantidade"},
                    "id_produto = ?",
                    new String[]{String.valueOf(produtoId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                estoque = new Estoque(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_produto")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantidade"))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar estoque por produto ID: " + produtoId + " - " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return estoque;
    }

    /**
     * Lista todas as entradas de estoque, juntando com a tabela Produto para obter o nome do produto.
     *
     * @return Uma lista de objetos Estoque, com o campo nomeProduto preenchido.
     */
    public List<Estoque> listarTodosComNomeProduto() {
        List<Estoque> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String query = "SELECT E.id, E.id_produto, P.nome AS nome_produto, E.quantidade " +
                "FROM Estoque E INNER JOIN Produto P ON E.id_produto = P.id " +
                "ORDER BY P.nome ASC";
        try {
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Estoque e = new Estoque(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("id_produto")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("quantidade"))
                    );
                    e.setNomeProduto(cursor.getString(cursor.getColumnIndexOrThrow("nome_produto")));
                    lista.add(e);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao listar todos os estoques com nome do produto: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return lista;
    }

    /**
     * Atualiza diretamente a quantidade de um produto no estoque.
     * Se o produto não tiver uma entrada de estoque, uma nova será criada.
     *
     * @param produtoId O ID do produto.
     * @param novaQuantidade A nova quantidade em estoque.
     * @return true se a operação foi bem-sucedida, false caso contrário.
     */
    public boolean atualizarQuantidade(int produtoId, int novaQuantidade) {
        Estoque estoque = buscarPorProdutoId(produtoId);
        if (estoque == null) { // Produto não tem entrada no estoque ainda
            estoque = new Estoque(produtoId, novaQuantidade); // O ID do estoque será auto-gerado
        } else {
            estoque.setQuantidade(novaQuantidade);
        }
        return inserirOuAtualizar(estoque) != -1;
    }

    /**
     * Exclui uma entrada de estoque baseada no ID do produto.
     * Útil quando um produto é removido do sistema.
     *
     * @param produtoId O ID do produto cuja entrada de estoque será removida.
     * @return O número de linhas afetadas (deve ser 1 se o produto estava no estoque, 0 caso contrário).
     */
    public int excluirPorProdutoId(int produtoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            linhasAfetadas = db.delete("Estoque", "id_produto = ?", new String[]{String.valueOf(produtoId)});
            if (linhasAfetadas > 0) {
                Log.d(TAG, "Estoque para produto ID: " + produtoId + " excluído.");
            } else {
                Log.w(TAG, "Nenhuma entrada de estoque encontrada para excluir para produto ID: " + produtoId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir estoque por produto ID: " + produtoId + " - " + e.getMessage(), e);
        }
        return linhasAfetadas;
    }

    /**
     * Exclui uma entrada de estoque pelo seu ID primário na tabela Estoque.
     *
     * @param idEstoque O ID da entrada de estoque a ser removida.
     * @return O número de linhas afetadas.
     */
    public int excluirPorIdEstoque(int idEstoque) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            linhasAfetadas = db.delete("Estoque", "id = ?", new String[]{String.valueOf(idEstoque)});
            if (linhasAfetadas > 0) {
                Log.d(TAG, "Entrada de estoque ID: " + idEstoque + " excluída.");
            } else {
                Log.w(TAG, "Nenhuma entrada de estoque encontrada com ID: " + idEstoque);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir estoque por ID: " + idEstoque + " - " + e.getMessage(), e);
        }
        return linhasAfetadas;
    }
}
