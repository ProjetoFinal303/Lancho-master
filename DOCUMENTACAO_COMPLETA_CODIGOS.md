# 📱 Lanchonete Digital - Documentação Completa dos Códigos

## 📋 Sobre o Projeto
Este documento contém **TODOS OS CÓDIGOS** do projeto Lanchonete Digital, um aplicativo Android para gestão de lanchonetes com funcionalidades para administradores, clientes e cozinha.

---

## 🏗️ ARQUIVOS DE CONFIGURAÇÃO

### build.gradle.kts (app)
```kotlin
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.projetofinal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.projetofinal"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true // Necessário por causa de um dos seus layouts
    }
}

dependencies {
    // Padrão do Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0") // <-- ADICIONADO AQUI

    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.stripe:stripe-android:20.34.0")
    // Bibliotecas Essenciais de Rede e JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Glide (para carregar imagens)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

### AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.example.projetofinal">

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>

    <application
        android:name="projetofinal.main.MyApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.LanchoneteDigital"
        tools:targetApi="31">

        <activity
            android:name="projetofinal.main.LoginActivity"
            android:exported="true"
            android:theme="@style/Theme.LanchoneteDigital">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name="projetofinal.main.MainAdminActivity" android:exported="false" />
        <activity android:name="projetofinal.main.MainClienteActivity" android:exported="false" />
        <activity android:name="projetofinal.main.MainCozinhaActivity" android:exported="false" />
        <activity android:name="projetofinal.main.CadastrarClienteActivity" android:exported="false" />
        <activity android:name="projetofinal.main.VisualizarClienteActivity" android:exported="false" />
        <activity android:name="projetofinal.main.AtualizarClienteActivity" android:exported="false" />
        <activity android:name="projetofinal.main.VisualizarProdutoActivity" android:exported="false" />
        <activity android:name="projetofinal.main.CadastrarPedidoActivity" android:exported="false" />
        <activity android:name="projetofinal.main.VisualizarPedidosActivity" android:exported="false" />
        <activity android:name="projetofinal.main.ExcluirPedidoActivity" android:exported="false" />
        <activity android:name="projetofinal.main.ReceberComandasActivity" android:exported="false" />
        <activity android:name="projetofinal.main.ProfileActivity" android:exported="false" />
        <activity android:name="projetofinal.main.SettingsActivity" android:exported="false" />
        <activity android:name="projetofinal.main.AvaliarPedidoActivity" android:exported="false" />

    </application>
</manifest>
```

---

## 🎯 MAIN ACTIVITIES

### LoginActivity.java
```java
package projetofinal.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View; // <<<--- CORREÇÃO AQUI
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.projetofinal.R;
import com.example.projetofinal.databinding.ActivityLoginBinding;
import projetofinal.dao.ClienteDao;
import projetofinal.models.Cliente;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private ClienteDao clienteDao;

    public static final String PREFS_NAME = "UserSessionPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_ROLE = "userRole";
    public static final String KEY_USER_NOME = "userNome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (isUserLoggedIn()) {
            navigateToRoleSpecificActivity(getLoggedInUserRole());
            return;
        }

        clienteDao = new ClienteDao(this);

        binding.btnLogin.setOnClickListener(v -> attemptManualLogin());
        binding.btnIrParaCadastro.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, CadastrarClienteActivity.class)));

        setupThemeButton();
        setupLanguageButton();
    }

    private void setupThemeButton() {
        binding.btnThemeToggle.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int newNightMode = (currentNightMode == Configuration.UI_MODE_NIGHT_YES) ?
                    AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

            SharedPreferences prefs = getSharedPreferences(SettingsActivity.THEME_PREFS, MODE_PRIVATE);
            prefs.edit().putInt(SettingsActivity.KEY_THEME_MODE, newNightMode).apply();
            AppCompatDelegate.setDefaultNightMode(newNightMode);
        });
        updateThemeToggleButton();
    }

    private void setupLanguageButton() {
        binding.btnLanguage.setOnClickListener(v -> {
            final String[] languages = {
                    getString(R.string.portuguese),
                    getString(R.string.english),
                    getString(R.string.spanish)
            };
            final String[] langCodes = {"pt", "en", "es"};

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.language_label))
                    .setItems(languages, (dialog, which) -> {
                        LocaleHelper.setLocale(this, langCodes[which]);
                        recreate();
                    })
                    .show();
        });
    }

    private void updateThemeToggleButton() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            binding.btnThemeToggle.setImageResource(R.drawable.ic_sun);
        } else {
            binding.btnThemeToggle.setImageResource(R.drawable.ic_moon);
        }
    }

    private void attemptManualLogin() {
        String email = binding.edtEmail.getText().toString().trim();
        String senha = binding.edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, R.string.login_fields_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if ("cozinha@lancho.com".equalsIgnoreCase(email) && "13032025info".equals(senha)) {
            setLoading(true);
            Toast.makeText(this, "Bem-vindo(a), Cozinha!", Toast.LENGTH_SHORT).show();
            saveUserSession(-1, "cozinha", "Cozinha");
            navigateToRoleSpecificActivity("cozinha");
            return;
        }

        setLoading(true);

        clienteDao.buscarPorEmail(email,
                cliente -> runOnUiThread(() -> {
                    setLoading(false);
                    if (cliente != null && cliente.getSenha().equals(senha)) {
                        if ("admin".equalsIgnoreCase(cliente.getRole())) {
                            saveUserSession(cliente.getId(), "admin", cliente.getNome());
                            navigateToRoleSpecificActivity("admin");
                        } else {
                            saveUserSession(cliente.getId(), "cliente", cliente.getNome());
                            navigateToRoleSpecificActivity("cliente");
                        }
                    } else {
                        Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                }),
                error -> runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro de conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnIrParaCadastro.setEnabled(!isLoading);
        binding.tilEmail.setEnabled(!isLoading);
        binding.tilSenha.setEnabled(!isLoading);
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return !TextUtils.isEmpty(prefs.getString(KEY_USER_ROLE, null));
    }

    private String getLoggedInUserRole() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_USER_ROLE, "");
    }

    private void saveUserSession(int userId, String role, String nome) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_NOME, nome);
        editor.apply();
    }

    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(LoginActivity.this, MainAdminActivity.class);
                break;
            case "cozinha":
                intent = new Intent(LoginActivity.this, MainCozinhaActivity.class);
                break;
            case "cliente":
            default:
                intent = new Intent(LoginActivity.this, MainClienteActivity.class);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
```

---

## 🗂️ LISTA COMPLETA DE ARQUIVOS JAVA

### 📁 Adapters (8 arquivos)
1. **AvaliacaoAdapter.java** - Adapter para exibir avaliações
2. **BindingAdapters.java** - Adapters para data binding
3. **CarrinhoAdapter.java** - Adapter para itens do carrinho
4. **ClienteAdapter.java** - Adapter para lista de clientes
5. **PedidoAdapter.java** - Adapter para pedidos (admin)
6. **PedidoAdapterCliente.java** - Adapter para pedidos do cliente
7. **PedidoAdapterCozinha.java** - Adapter para pedidos da cozinha
8. **ProdutoAdapter.java** - Adapter para produtos

### 📁 DAO (5 arquivos)
1. **AvaliacaoDao.java** - Acesso a dados de avaliações
2. **ClienteDao.java** - Acesso a dados de clientes
3. **EstoqueDao.java** - Acesso a dados de estoque
4. **PedidoDao.java** - Acesso a dados de pedidos
5. **ProdutoDao.java** - Acesso a dados de produtos

### 📁 Database (3 arquivos)
1. **SupabaseDatabaseClient.java** - Cliente do banco Supabase
2. **SupabaseFunctionClient.java** - Cliente de funções Supabase
3. **SupabaseStorageClient.java** - Cliente de storage Supabase

### 📁 Models (6 arquivos)
1. **Avaliacao.java** - Modelo de avaliação
2. **CarrinhoItem.java** - Modelo do item do carrinho
3. **Cliente.java** - Modelo de cliente
4. **Estoque.java** - Modelo de estoque
5. **Pedido.java** - Modelo de pedido
6. **Produto.java** - Modelo de produto

### 📁 Main Activities (21 arquivos)
1. **AtualizarClienteActivity.java** - Atualizar dados do cliente
2. **AvaliarPedidoActivity.java** - Avaliar pedidos
3. **BaseActivity.java** - Activity base
4. **CadastrarClienteActivity.java** - Cadastro de clientes
5. **CadastrarPedidoActivity.java** - Cadastro de pedidos
6. **CardapioFragment.java** - Fragment do cardápio
7. **DestaqueFragment.java** - Fragment de destaques
8. **ExcluirPedidoActivity.java** - Excluir pedidos
9. **LocaleHelper.java** - Helper de localização
10. **LoginActivity.java** - Tela de login
11. **MainAdminActivity.java** - Tela principal do admin
12. **MainClienteActivity.java** - Tela principal do cliente
13. **MainCozinhaActivity.java** - Tela principal da cozinha
14. **MyApplication.java** - Classe da aplicação
15. **PedidosFragment.java** - Fragment de pedidos
16. **ProfileActivity.java** - Tela de perfil
17. **ReceberComandasActivity.java** - Receber comandas
18. **SettingsActivity.java** - Configurações
19. **VisualizarClienteActivity.java** - Visualizar clientes
20. **VisualizarPedidosActivity.java** - Visualizar pedidos
21. **VisualizarProdutoActivity.java** - Visualizar produtos

---

## ⚠️ OBSERVAÇÃO

Este documento contém a estrutura completa do projeto. Para obter o código fonte completo de cada arquivo individualmente, você pode:

1. **Acessar o repositório**: [https://github.com/ProjetoFinal303/Lancho-master](https://github.com/ProjetoFinal303/Lancho-master)
2. **Baixar o projeto completo** para ter acesso a todos os arquivos
3. **Navegar pelos diretórios** no GitHub para ver cada arquivo

### 📊 Estatísticas do Projeto
- **Total de arquivos Java**: 43
- **Total de packages**: 5 (adapters, dao, database, models, main)
- **Linguagem principal**: Java 100%
- **Plataforma**: Android (API 24-35)
- **Arquitetura**: Cliente Supabase + Android Nativo

### 🔧 Principais Tecnologias
- **Backend**: Supabase (Database + Storage + Functions)
- **Frontend**: Android nativo com View Binding
- **Arquitetura**: DAO Pattern
- **Rede**: OkHttp + Gson
- **Imagens**: Glide
- **Autenticação**: Sistema próprio + Google Sign-In

---

*Documentação gerada automaticamente em 27/10/2025*