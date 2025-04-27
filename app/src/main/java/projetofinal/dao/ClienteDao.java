package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import projetofinal.database.DatabaseHelper;
import projetofinal.models.Cliente;

public class ClienteDao {
    private final DatabaseHelper dbHelper;

    public ClienteDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
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
            if (cliente == null) {
                Log.e("ClienteDao", "Cliente não encontrado com o email: " + email);
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return cliente;
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
            if (cliente == null) {
                Log.e("ClienteDao", "Cliente não encontrado com o ID: " + id);
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return cliente;
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

    // Excluir cliente por ID
    public void excluir(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete("Cliente", "id = ?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    // Obter todos os clientes
    public List<Cliente> getAllClientes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Cliente> clientes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Cliente", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Cliente cliente = new Cliente(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("nome")),
                            cursor.getString(cursor.getColumnIndex("email")),
                            cursor.getString(cursor.getColumnIndex("contato")),
                            cursor.getString(cursor.getColumnIndex("senha"))
                    );
                    clientes.add(cliente);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return clientes;
    }
}
