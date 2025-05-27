package projetofinal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lanchonete.db";

    private static final int DATABASE_VERSION = 6;
    private static final String TAG = "DatabaseHelper";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Criando tabelas do banco de dados, versão: " + DATABASE_VERSION);
        String createClienteTable = "CREATE TABLE Cliente (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "contato TEXT NOT NULL, " +
                "senha TEXT NOT NULL)";

        // Garanta que 'nome' seja UNIQUE para evitar produtos duplicados com mesmo nome
        String createProdutoTable = "CREATE TABLE Produto (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL UNIQUE, " +
                "descricao TEXT, " +
                "preco REAL NOT NULL)"; // Preço ainda como REAL no SQLite, BigDecimal é tratado no Java

        String createPedidoTable = "CREATE TABLE Pedido (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_cliente INTEGER NOT NULL, " +
                "descricao TEXT NOT NULL, " +
                "valor REAL NOT NULL, " +      // Valor ainda como REAL no SQLite
                "data TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "FOREIGN KEY(id_cliente) REFERENCES Cliente(id) ON DELETE CASCADE)";

        String createEstoqueTable = "CREATE TABLE Estoque (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_produto INTEGER NOT NULL UNIQUE, " +
                "quantidade INTEGER NOT NULL DEFAULT 0, " + // Quantidade padrão 0
                "FOREIGN KEY(id_produto) REFERENCES Produto(id) ON DELETE CASCADE)";

        db.execSQL(createClienteTable);
        Log.d(TAG, "Tabela Cliente criada.");
        db.execSQL(createProdutoTable);
        Log.d(TAG, "Tabela Produto criada.");
        db.execSQL(createPedidoTable);
        Log.d(TAG, "Tabela Pedido criada.");
        db.execSQL(createEstoqueTable);
        Log.d(TAG, "Tabela Estoque criada.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Atualizando banco de dados da versão " + oldVersion + " para " + newVersion + ". Dados antigos serão perdidos.");
        // Política simples de upgrade: dropar e recriar.
        // Para produção, implemente uma migração de dados adequada.
        db.execSQL("DROP TABLE IF EXISTS Pedido");
        db.execSQL("DROP TABLE IF EXISTS Estoque");
        db.execSQL("DROP TABLE IF EXISTS Produto");
        db.execSQL("DROP TABLE IF EXISTS Cliente");
        onCreate(db);
    }

    // Opcional: Habilitar chaves estrangeiras se não estiverem por padrão (geralmente estão)
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Habilita o suporte a chaves estrangeiras.
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}