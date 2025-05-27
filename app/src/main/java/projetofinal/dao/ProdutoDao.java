package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Produto;

public class ProdutoDao {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "ProdutoDao";

    public ProdutoDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long inserir(Produto produto) {
        if (produto == null || TextUtils.isEmpty(produto.getNome())) {
            Log.e(TAG, "INSERIR: Tentativa de inserir produto nulo ou sem nome.");
            return -1;
        }

        Produto existente = buscarPorNome(produto.getNome());
        if (existente != null) {
            Log.w(TAG, "INSERIR: Produto com nome '" + produto.getNome() + "' já existe (ID: " + existente.getId() + "). Não será inserido novamente.");
            return -2; // Código para nome duplicado
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put("nome", produto.getNome());
            values.put("descricao", produto.getDescricao());
            values.put("preco", produto.getPreco() != null ? produto.getPreco().doubleValue() : 0.0);

            id = db.insertOrThrow("Produto", null, values); // Usar insertOrThrow para pegar exceções do SQLite

            if (id != -1) {
                Log.i(TAG, "INSERIR: Produto inserido com SUCESSO. ID: " + id + ", Nome: " + produto.getNome());
            }
        } catch (Exception e) {
            Log.e(TAG, "INSERIR: Erro ao inserir produto '" + produto.getNome() + "': " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique constraint failed: produto.nome")) {
                return -2; // Retorna código de nome duplicado
            }
            return -1;
        }
        return id;
    }

    public Produto buscarPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Produto produto = null;
        Cursor cursor = null;
        try {
            cursor = db.query("Produto",
                    new String[]{"id", "nome", "descricao", "preco"},
                    "id = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                produto = new Produto(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("preco")) // Lê como double
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar produto por ID: " + id + " - " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return produto;
    }

    public Produto buscarPorNome(String nome) {
        if (TextUtils.isEmpty(nome)) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Produto produto = null;
        Cursor cursor = null;
        try {
            cursor = db.query("Produto",
                    new String[]{"id", "nome", "descricao", "preco"},
                    "nome = ? COLLATE NOCASE",
                    new String[]{nome},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                produto = new Produto(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("preco"))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar produto por nome: " + nome + " - " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return produto;
    }

    public List<Produto> listarTodos() {
        List<Produto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Log.d(TAG, "LISTAR TODOS: Iniciando busca...");
        try {
            cursor = db.rawQuery("SELECT id, nome, descricao, preco FROM Produto ORDER BY nome ASC", null);
            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "LISTAR TODOS: Cursor OK, " + cursor.getCount() + " linhas.");
                do {
                    Produto p = new Produto(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                            cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("preco"))
                    );
                    lista.add(p);
                    Log.v(TAG, "LISTAR TODOS: Adicionado produto: " + p.getNome() + " (ID: " + p.getId() + ")");
                } while (cursor.moveToNext());
            } else {
                Log.w(TAG, "LISTAR TODOS: Nenhum produto encontrado no banco ou cursor nulo.");
            }
        } catch (Exception e) {
            Log.e(TAG, "LISTAR TODOS: Erro: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        Log.i(TAG, "LISTAR TODOS: Finalizado. Total de produtos: " + lista.size());
        return lista;
    }

    public int atualizar(Produto produto) {
        if (produto == null) return 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("nome", produto.getNome());
            values.put("descricao", produto.getDescricao());
            values.put("preco", produto.getPreco() != null ? produto.getPreco().doubleValue() : 0.0);
            linhasAfetadas = db.update("Produto", values, "id = ?", new String[]{String.valueOf(produto.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar produto ID " + produto.getId() + ": " + e.getMessage(), e);
        }
        return linhasAfetadas;
    }

    public int excluir(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            linhasAfetadas = db.delete("Produto", "id = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir produto ID " + id + ": " + e.getMessage(), e);
        }
        return linhasAfetadas;
    }
}