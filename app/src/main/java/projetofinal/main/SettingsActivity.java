package projetofinal.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    public static final String THEME_PREFS = "ThemePrefs";
    public static final String KEY_THEME_MODE = "ThemeMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarSettings;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        updateThemeSummary();
        binding.optionTheme.setOnClickListener(v -> showThemeDialog());
    }

    private void showThemeDialog() {
        String[] themes = { "Claro", "Escuro", "Padrão do Sistema" };
        int currentNightMode = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
                .getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int checkedItem = 2; // Padrão do Sistema
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            checkedItem = 0; // Claro
        } else if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 1; // Escuro
        }

        new AlertDialog.Builder(this)
                .setTitle("Escolher Tema")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int selectedThemeMode;
                    switch (which) {
                        case 0: // Claro
                            selectedThemeMode = AppCompatDelegate.MODE_NIGHT_NO;
                            break;
                        case 1: // Escuro
                            selectedThemeMode = AppCompatDelegate.MODE_NIGHT_YES;
                            break;
                        default: // Sistema
                            selectedThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                            break;
                    }
                    saveThemeSetting(selectedThemeMode);
                    AppCompatDelegate.setDefaultNightMode(selectedThemeMode);
                    updateThemeSummary();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateThemeSummary() {
        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        int currentNightMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        String themeName;
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            themeName = "Claro";
        } else if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeName = "Escuro";
        } else {
            themeName = "Padrão do Sistema";
        }
        binding.textViewCurrentTheme.setText(themeName);
    }

    private void saveThemeSetting(int themeMode) {
        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}