package projetofinal.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    public static final String THEME_PREFS = "ThemePrefs";
    public static final String KEY_THEME_MODE = "ThemeMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarSettings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarSettings.setNavigationOnClickListener(v -> onBackPressed());

        setupThemeSpinner();
    }

    private void setupThemeSpinner() {
        Spinner spinner = binding.spinnerTheme;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.theme_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        int currentThemeMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Define a seleção inicial do spinner
        switch (currentThemeMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                spinner.setSelection(0); // Claro
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                spinner.setSelection(1); // Escuro
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                spinner.setSelection(2); // Sistema
                break;
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedMode;
                switch (position) {
                    case 0: // Claro
                        selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                    case 1: // Escuro
                        selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
                        break;
                    case 2: // Sistema
                    default:
                        selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        break;
                }

                // Salva e aplica o tema se for diferente do atual
                if (AppCompatDelegate.getDefaultNightMode() != selectedMode) {
                    AppCompatDelegate.setDefaultNightMode(selectedMode);
                    saveThemePreference(selectedMode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Não faz nada
            }
        });
    }

    private void saveThemePreference(int mode) {
        SharedPreferences prefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_THEME_MODE, mode);
        editor.apply();
    }
}