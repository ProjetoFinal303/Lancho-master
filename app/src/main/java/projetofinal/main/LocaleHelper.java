package projetofinal.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private static final String LANGUAGE_PREFS = "LanguagePrefs";

    // Define o idioma e o salva nas preferências
    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    // Pega o idioma salvo
    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    // Salva o idioma escolhido
    private static void persist(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    // Atualiza a configuração do app com o novo idioma
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    // Método a ser chamado na BaseActivity para garantir que o idioma seja aplicado
    public static void onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        setLocale(context, lang);
    }
}
