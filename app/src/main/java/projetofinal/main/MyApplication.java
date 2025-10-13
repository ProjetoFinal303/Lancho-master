package projetofinal.main;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Esta linha FORÇA o aplicativo a iniciar sempre no modo claro (dia).
        // Assim que o app abrir, essa regra será aplicada antes de qualquer tela aparecer.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}