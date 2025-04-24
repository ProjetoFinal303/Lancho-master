package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Cliente;

public class ClienteDao {
    private final DatabaseHelper dbHelper;

    public ClienteDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);  // Chamada corrigida para o Singleton
    }

    // Inserir novo cliente
    public void inserir(Cliente cliente) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("nome", cliente.getNome());
            values.put("email", cliente.getEmail());
            values.put("contato", cliente.getContato());
            values.put("senha", cliente.getSenha());
            db.insertWithOnConflict("Cliente", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        } finally {
            db.close();
        }
    }

    // Atualizar dados de cliente
    public int atualizar(Cliente cliente) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("nome", cliente.getNome());
            values.put("email", cliente.getEmail());
            values.put("contato", cliente.getContato());
            values.put("senha", cliente.getSenha());
            linhasAfetadas = db.update("Cliente", values, "id = ?", new String[]{String.valueOf(cliente.getId())});
        } finally {
            db.close();
        }
        return linhasAfetadas;
    }

    // Excluir cliente
    public void excluir(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete("Cliente", "id = ?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    // Listar todos os clientes
    public List<Cliente> getAllClientes() {
        List<Cliente> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Cliente ORDER BY nome ASC", null);
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    String nome = cursor.getString(cursor.getColumnIndex("nome"));
                    String email = cursor.getString(cursor.getColumnIndex("email"));
                    String contato = cursor.getString(cursor.getColumnIndex("contato"));
                    String senha = cursor.getString(cursor.getColumnIndex("senha"));

                    Cliente cliente = new Cliente(id, nome, email, contato, senha);
                    lista.add(cliente);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return lista;
    }

    // Buscar cliente por ID
    public Cliente buscarPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cliente cliente = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Cliente WHERE id = ?", new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToFirst()) {
                cliente = new Cliente(
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("nome")),
                        cursor.getString(cursor.getColumnIndex("email")),
                        cursor.getString(cursor.getColumnIndex("contato")),
                        cursor.getString(cursor.getColumnIndex("senha"))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return cliente;
    }

    // Buscar cliente por EMAIL
    public Cliente buscarPorEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cliente cliente = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Cliente WHERE email = ?", new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                cliente = new Cliente(
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("nome")),
                        cursor.getString(cursor.getColumnIndex("email")),
                        cursor.getString(cursor.getColumnIndex("contato")),
                        cursor.getString(cursor.getColumnIndex("senha"))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return cliente;
    }
}
