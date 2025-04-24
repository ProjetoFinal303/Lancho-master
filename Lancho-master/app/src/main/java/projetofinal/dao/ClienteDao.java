package projetofinal.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import projetofinal.database.DatabaseHelper;
import projetofinal.models.Cliente;

import java.util.ArrayList;
import java.util.List;

public class ClienteDao {

    private final DatabaseHelper dbHelper;

    public ClienteDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // Inserir cliente
    public void inserir(Cliente cliente) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("id", cliente.getId());
            values.put("nome", cliente.getNome());
            values.put("contato", cliente.getContato());

            db.insertWithOnConflict("Cliente", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        } finally {
            db.close();
        }
    }

    // Atualizar cliente
    public int atualizar(Cliente cliente) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("nome", cliente.getNome());
            values.put("contato", cliente.getContato());

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
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex("id");
                    int nomeIndex = cursor.getColumnIndex("nome");
                    int contatoIndex = cursor.getColumnIndex("contato");

                    if (idIndex != -1 && nomeIndex != -1 && contatoIndex != -1) {
                        lista.add(new Cliente(
                                cursor.getInt(idIndex),
                                cursor.getString(nomeIndex),
                                cursor.getString(contatoIndex)
                        ));
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return lista;
    }

    // Buscar cliente por id
    public Cliente buscarPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cliente cliente = null;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM Cliente WHERE id = ?", new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                int nomeIndex = cursor.getColumnIndex("nome");
                int contatoIndex = cursor.getColumnIndex("contato");

                if (idIndex != -1 && nomeIndex != -1 && contatoIndex != -1) {
                    cliente = new Cliente(
                            cursor.getInt(idIndex),
                            cursor.getString(nomeIndex),
                            cursor.getString(contatoIndex)
                    );
                }
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return cliente;
    }

    // Verificar se o cliente existe(retorna um boolean)
    public boolean clienteExiste(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean existe = false;

        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM Cliente WHERE id = ?", new String[]{String.valueOf(id)});
            if (cursor != null && cursor.moveToFirst()) {
                existe = cursor.getInt(0) > 0;
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return existe;
    }
}
