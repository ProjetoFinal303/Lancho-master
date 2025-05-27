package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Pedido;

public class PedidoDao {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "PedidoDao";

    public PedidoDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public long inserir(Pedido pedido) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            // ID do pedido é autoincrementável, nao precisa colocar aqui explicitamente
            // values.put("id", pedido.getId()); // Apenas se o ID nao for auto-incrementado
            values.put("id_cliente", pedido.getClienteId());
            values.put("descricao", pedido.getDescricao());
            values.put("valor", pedido.getValor());
            values.put("data", pedido.getData() != null ? pedido.getData() : getCurrentDateTime());
            values.put("status", pedido.getStatus() != null ? pedido.getStatus() : "Pendente"); // Status padrao

            id = db.insertWithOnConflict("Pedido", null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                Log.e(TAG, "Falha ao inserir pedido.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inserir pedido: " + e.getMessage());
        }
        return id;
    }

    public int excluir(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            linhasAfetadas = db.delete("Pedido", "id = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir pedido: " + e.getMessage());
        }
        return linhasAfetadas;
    }

    public int atualizarStatus(int pedidoId, String novoStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("status", novoStatus);
            linhasAfetadas = db.update("Pedido", values, "id = ?", new String[]{String.valueOf(pedidoId)});
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar status do pedido: " + e.getMessage());
        }
        return linhasAfetadas;
    }


    public List<Pedido> listarTodos() {
        List<Pedido> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String query = "SELECT id, id_cliente, descricao, valor, data, status FROM Pedido ORDER BY data DESC";

        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    lista.add(new Pedido(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("id_cliente")),
                            cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                            cursor.getString(cursor.getColumnIndexOrThrow("data")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao listar todos os pedidos: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return lista;
    }

    public List<Pedido> buscarPedidosPorClienteId(int clienteId) {
        List<Pedido> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String query = "SELECT id, id_cliente, descricao, valor, data, status FROM Pedido WHERE id_cliente = ? ORDER BY data DESC";

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(clienteId)});
            if (cursor.moveToFirst()) {
                do {
                    lista.add(new Pedido(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("id_cliente")),
                            cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                            cursor.getString(cursor.getColumnIndexOrThrow("data")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar pedidos por cliente ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return lista;
    }

    public Pedido buscarPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Pedido pedido = null;
        Cursor cursor = null;
        String query = "SELECT id, id_cliente, descricao, valor, data, status FROM Pedido WHERE id = ?";
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToFirst()) {
                pedido = new Pedido(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_cliente")),
                        cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                        cursor.getString(cursor.getColumnIndexOrThrow("data")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar pedido por ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return pedido;
    }
}