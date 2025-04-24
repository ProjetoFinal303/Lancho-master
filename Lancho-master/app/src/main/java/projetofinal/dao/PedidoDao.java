package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Pedido;

import java.util.ArrayList;
import java.util.List;

public class PedidoDao {
    private final DatabaseHelper dbHelper;

    public PedidoDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public void inserir(Pedido pedido) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();s
            values.put("id", pedido.getId());
            values.put("id_cliente", pedido.getClienteId());
            values.put("descricao", pedido.getDescricao());
            values.put("valor", pedido.getValor());

            db.insertWithOnConflict("Pedido", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        } finally {
            db.close();
        }
    }

    public void excluir(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete("Pedido", "id = ?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    public List<Pedido> listarTodos() {
        List<Pedido> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT id, id_cliente, descricao, valor FROM Pedido", null);
            if (cursor.moveToFirst()) {
                do {
                    lista.add(new Pedido(
                            cursor.getInt(0),  // id
                            cursor.getInt(1),  // Cliente id
                            cursor.getString(2),  // Descrição
                            cursor.getDouble(3)  // Valor
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return lista;
    }
}
