package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Estoque;

import java.util.ArrayList;
import java.util.List;

public class EstoqueDao {
    private final DatabaseHelper dbHelper;

    public EstoqueDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public void inserir(Estoque estoque) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("id_produto", estoque.getProdutoId());
            values.put("quantidade", estoque.getQuantidade());
            db.insert("Estoque", null, values);
        } finally {
            db.close();
        }
    }

    public List<Estoque> listarTodos() {
        List<Estoque> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Estoque", null);
            if (cursor.moveToFirst()) {
                do {
                    Estoque e = new Estoque(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getInt(cursor.getColumnIndex("id_produto")),
                            cursor.getInt(cursor.getColumnIndex("quantidade"))
                    );
                    lista.add(e);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return lista;
    }
}
