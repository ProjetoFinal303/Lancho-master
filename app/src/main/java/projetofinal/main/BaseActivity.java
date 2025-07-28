package projetofinal.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Aplica o tema ANTES do onCreate da activity filha
        applyAppTheme();
        super.onCreate(savedInstanceState);
    }

    private void applyAppTheme() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.THEME_PREFS, MODE_PRIVATE);
        int themeMode = prefs.getInt(SettingsActivity.KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (AppCompatDelegate.getDefaultNightMode() != themeMode) {
            AppCompatDelegate.setDefaultNightMode(themeMode);
        }
    }
}