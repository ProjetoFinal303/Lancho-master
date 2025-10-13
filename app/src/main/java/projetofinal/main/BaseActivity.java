package projetofinal.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseActivity extends AppCompatActivity {

    // A mágica acontece aqui: este métdo é chamado ANTES do onCreate
    @Override
    protected void attachBaseContext(Context newBase) {
        // Aplica o idioma salvo antes de a tela ser construída
        super.attachBaseContext(LocaleHelper.setLocale(newBase, getSavedLanguage(newBase)));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // A lógica do tema continua aqui, mas não é mais a primeira coisa a ser executada
        applyAppTheme();
    }

    private void applyAppTheme() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.THEME_PREFS, MODE_PRIVATE);
        int themeMode = prefs.getInt(SettingsActivity.KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        if (AppCompatDelegate.getDefaultNightMode() != themeMode) {
            AppCompatDelegate.setDefaultNightMode(themeMode);
        }
    }

    // Pega o idioma salvo para aplicar
    private String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("LanguagePrefs", MODE_PRIVATE);
        // O padrão agora é 'pt' (Português)
        return prefs.getString("Locale.Helper.Selected.Language", "pt");
    }
}
