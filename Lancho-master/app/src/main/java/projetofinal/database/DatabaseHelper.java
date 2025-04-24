package projetofinal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import projetofinal.dao.ClienteDao;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lanchonete.db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;
    private final Context appContext;
    private ClienteDao clienteDao;

    // Construtor privado para evitar instâncias diretas
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
    }

    // Método Singleton para obter a instância única do banco de dados
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createClienteTable = "CREATE TABLE Cliente (" +
                "id INTEGER PRIMARY KEY, " +
                "nome TEXT NOT NULL, " +
                "contato TEXT NOT NULL)";

        String createPedidoTable = "CREATE TABLE Pedido (" +
                "id INTEGER PRIMARY KEY, " +
                "data TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "id_cliente INTEGER NOT NULL, " +
                "FOREIGN KEY(id_cliente) REFERENCES Cliente(id) ON DELETE CASCADE)";

        db.execSQL(createClienteTable);
        db.execSQL(createPedidoTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Cliente");
        db.execSQL("DROP TABLE IF EXISTS Pedido");
        onCreate(db);
    }

    // Método para acessar o ClienteDao
    public ClienteDao clienteDao() {
        if (clienteDao == null) {
            clienteDao = new ClienteDao(appContext);
        }
        return clienteDao;
}
}
