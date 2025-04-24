package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Produto;

public class ProdutoDao {
    private final DatabaseHelper dbHelper;

    public ProdutoDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public void inserir(Produto produto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("nome", produto.getNome());
            values.put("descricao", produto.getDescricao());
            values.put("preco", produto.getPreco());
            db.insert("Produto", null, values);
        } finally {
            db.close();
        }
    }

    public List<Produto> listarTodos() {
        List<Produto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Produto", null);
            if (cursor.moveToFirst()) {
                do {
                    Produto p = new Produto(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("nome")),
                            cursor.getString(cursor.getColumnIndex("descricao")),
                            cursor.getDouble(cursor.getColumnIndex("preco"))
                    );
                    lista.add(p);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return lista;
    }
}
