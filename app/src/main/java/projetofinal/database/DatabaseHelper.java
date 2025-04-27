package projetofinal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lanchonete.db";
    private static final int DATABASE_VERSION = 4;

    private static DatabaseHelper instance;
    private final Context appContext;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createClienteTable = "CREATE TABLE Cliente (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "contato TEXT NOT NULL, " +
                "senha TEXT NOT NULL)";

        String createPedidoTable = "CREATE TABLE Pedido (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "data TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "id_cliente INTEGER NOT NULL, " +
                "FOREIGN KEY(id_cliente) REFERENCES Cliente(id) ON DELETE CASCADE)";

        String createProdutoTable = "CREATE TABLE Produto (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "descricao TEXT NOT NULL, " +
                "preco REAL NOT NULL)";

        db.execSQL(createProdutoTable);
        db.execSQL(createClienteTable);
        db.execSQL(createPedidoTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS Cliente");
            db.execSQL("DROP TABLE IF EXISTS Pedido");
            onCreate(db);
        }
    }
}
