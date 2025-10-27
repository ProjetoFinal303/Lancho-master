package projetofinal.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast; // Import Toast
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivitySettingsBinding;
import java.util.Locale;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    public static final String THEME_PREFS = "ThemePrefs";
    public static final String KEY_THEME_MODE = "ThemeMode";
    // Constantes de notificação removidas
    // public static final String NOTIFICATION_PREFS = "NotificationPrefs";
    // public static final String KEY_ORDER_NOTIFICATIONS = "OrderNotifications";
    // public static final String KEY_PROMO_NOTIFICATIONS = "PromoNotifications";
    private static final String TAG = "SettingsActivity"; // Tag for logging

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

        loadSettings(); // Carrega o estado atual das configurações
        setupClickListeners(); // Configura os cliques
    }

    private void loadSettings() {
        updateThemeSummary();
        updateLanguageSummary();
        // Carregamento do estado dos switches de notificação removido
    }

    private void setupClickListeners() {
        binding.optionTheme.setOnClickListener(v -> showThemeDialog());
        binding.optionLanguage.setOnClickListener(v -> showLanguageDialog());

        // Listeners para os switches de notificação removidos

        binding.optionAboutUs.setOnClickListener(v -> openUrl("https://lancho.vercel.app/sobre.html"));
        binding.optionWebsite.setOnClickListener(v -> openUrl("https://lancho.vercel.app/index.html"));
    }

    // --- Lógica para Tema ---
    private void showThemeDialog() {
        final String[] themes = getResources().getStringArray(R.array.theme_options);
        int currentNightMode = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
                .getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int checkedItem = 2; // Padrão do Sistema
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            checkedItem = 0; // Claro
        } else if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 1; // Escuro
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.theme_label)
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int selectedThemeMode;
                    switch (which) {
                        case 0: selectedThemeMode = AppCompatDelegate.MODE_NIGHT_NO; break;
                        case 1: selectedThemeMode = AppCompatDelegate.MODE_NIGHT_YES; break;
                        default: selectedThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; break;
                    }
                    saveThemeSetting(selectedThemeMode);
                    AppCompatDelegate.setDefaultNightMode(selectedThemeMode);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateThemeSummary() {
        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        int currentNightMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        String themeName;
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            themeName = getString(R.string.theme_light);
        } else if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeName = getString(R.string.theme_dark);
        } else {
            themeName = getString(R.string.theme_system);
        }
        binding.textViewCurrentTheme.setText(themeName);
    }

    private void saveThemeSetting(int themeMode) {
        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    // --- Lógica para Idioma ---
    private void showLanguageDialog() {
        final String[] languages = {
                getString(R.string.portuguese),
                getString(R.string.english),
                getString(R.string.spanish)
        };
        final String[] langCodes = {"pt", "en", "es"};
        String currentLangCode = LocaleHelper.getPersistedLocale(this);

        int checkedItem = 0; // Padrão Português
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLangCode)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.language_dialog_title)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLangCode = langCodes[which];
                    if (!selectedLangCode.equals(LocaleHelper.getPersistedLocale(this))) {
                        Log.d(TAG, "Idioma selecionado: " + selectedLangCode);
                        LocaleHelper.setLocale(this, selectedLangCode);
                        recreate();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateLanguageSummary() {
        String currentLangCode = LocaleHelper.getPersistedLocale(this);
        String langName;
        switch (currentLangCode) {
            case "en": langName = getString(R.string.english); break;
            case "es": langName = getString(R.string.spanish); break;
            case "pt":
            default: langName = getString(R.string.portuguese); break;
        }
        binding.textViewCurrentLanguage.setText(langName);
    }

    // --- Lógica para Notificações (Removida) ---
     /*
     private void saveNotificationSetting(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(NOTIFICATION_PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
     }
     */

    // --- Lógica para Abrir URLs ---
    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir URL: " + url, e);
            Toast.makeText(this, "Não foi possível abrir o link.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Volta para a tela anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // A classe interna LocaleHelper permanece a mesma
    public static class LocaleHelper {
        private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
        private static final String LANGUAGE_PREFS = "LanguagePrefs";

        public static Context setLocale(Context context, String language) {
            persist(context, language);
            return updateResources(context, language);
        }

        public static String getPersistedLocale(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE);
            return preferences.getString(SELECTED_LANGUAGE, Locale.getDefault().getLanguage());
        }

        private static void persist(Context context, String language) {
            SharedPreferences preferences = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SELECTED_LANGUAGE, language);
            editor.apply();
        }

        private static Context updateResources(Context context, String language) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);

            Resources res = context.getResources();
            Configuration config = new Configuration(res.getConfiguration());
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
            return context;
        }

        public static void onAttach(Context context) {
            String lang = getPersistedLocale(context);
            setLocale(context, lang);
        }
    }
}