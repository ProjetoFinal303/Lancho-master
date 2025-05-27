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
    private static final String TAG = "ClienteDao";

    public ClienteDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long inserir(Cliente cliente) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put("nome", cliente.getNome());
            values.put("email", cliente.getEmail());
            values.put("contato", cliente.getContato());
            values.put("senha", cliente.getSenha());
            id = db.insertWithOnConflict("Cliente", null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {
                Log.e(TAG, "Falha ao inserir cliente com email: " + cliente.getEmail() + ". Email pode já existir.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inserir cliente: " + e.getMessage());
        } finally {

        }
        return id;
    }

    public Cliente buscarPorEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cliente cliente = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Cliente WHERE email = ?", new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                cliente = new Cliente(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("contato")),
                        cursor.getString(cursor.getColumnIndexOrThrow("senha"))
                );
            } else {
                Log.d(TAG, "Cliente não encontrado com o email: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar cliente por email: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            // db.close();
        }
        return cliente;
    }

    public Cliente buscarPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cliente cliente = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Cliente WHERE id = ?", new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToFirst()) {
                cliente = new Cliente(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("contato")),
                        cursor.getString(cursor.getColumnIndexOrThrow("senha"))
                );
            } else {
                Log.d(TAG, "Cliente não encontrado com o ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar cliente por ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            // db.close();
        }
        return cliente;
    }

    public int atualizar(Cliente cliente) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("nome", cliente.getNome());
            values.put("email", cliente.getEmail());
            values.put("contato", cliente.getContato());
            values.put("senha", cliente.getSenha()); // Senha deve ser re-hashed se alterada
            linhasAfetadas = db.update("Cliente", values, "id = ?", new String[]{String.valueOf(cliente.getId())});
            if (linhasAfetadas == 0) {
                Log.w(TAG, "Nenhum cliente atualizado com ID: " + cliente.getId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar cliente: " + e.getMessage());
        } finally {
            // db.close();
        }
        return linhasAfetadas;
    }

    public int excluir(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            linhasAfetadas = db.delete("Cliente", "id = ?", new String[]{String.valueOf(id)});
            if (linhasAfetadas == 0) {
                Log.w(TAG, "Nenhum cliente excluído com ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao excluir cliente: " + e.getMessage());
        } finally {
            // db.close();
        }
        return linhasAfetadas;
    }

    public List<Cliente> getAllClientes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Cliente> clientes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM Cliente ORDER BY nome ASC", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Cliente cliente = new Cliente(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                            cursor.getString(cursor.getColumnIndexOrThrow("email")),
                            cursor.getString(cursor.getColumnIndexOrThrow("contato")),
                            cursor.getString(cursor.getColumnIndexOrThrow("senha"))
                    );
                    clientes.add(cliente);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar todos os clientes: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            // db.close();
        }
        return clientes;
    }
}