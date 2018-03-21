package com.example.eventos;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class EventosWeb extends AppCompatActivity {
    WebView navegador;
    ProgressDialog dialogo;
    String evento;
    final InterfazComunicacion miInterfazJava = new InterfazComunicacion(this);

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventos_web);

        Bundle extras = getIntent().getExtras();
        evento = extras.getString("evento");

        navegador = (WebView) findViewById(R.id.webkit);
        navegador.getSettings().setJavaScriptEnabled(true);
        navegador.getSettings().setBuiltInZoomControls(false);

        ActivityCompat.requestPermissions(
                EventosWeb.this, new String[]{
                        android.Manifest.permission.ACCESS_NETWORK_STATE
                }, 2
        );

        // en linea
        navegador.loadUrl("https://eventos-98475.firebaseapp.com/index.html");

        // fuera de linea
//        navegador.loadUrl("file:///android_asset/index.html");

//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri uri = Uri.parse("https://eventos-98475.firebaseapp.com/index.html");
//        intent.setData(uri);
//        startActivity(intent);

        navegador.setWebViewClient(new WebViewClient() {
            // contenido dentro de la web
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String url_filtro = "http://www.androidcurso.com/";
                if (!url.toString().equals(url_filtro)) {
                    view.loadUrl(url_filtro);
                }
                return false;
            }

            // dialogo de progreso
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                dialogo = new ProgressDialog(EventosWeb.this);
                dialogo.setMessage("Cargando...");
                dialogo.setCancelable(true);
                dialogo.show();
                if (!comprobarConectividad()) {
                    dialogo.dismiss();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                dialogo.dismiss();
                navegador.loadUrl("javascript:muestraEvento(\"" + evento + "\");");
            }

            // mensajes de error
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EventosWeb.this);
                builder.setMessage(description).setPositiveButton("Aceptar", null).setTitle("onReceivedError");
                builder.show();
            }
        });
        // mensajes de javascript
        navegador.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(EventosWeb.this)
                        .setTitle("Mensaje")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        }).setCancelable(false).create().show();
                return true;
            }
        });

        navegador.addJavascriptInterface(miInterfazJava, "jsInterfazNativa");
    }

    @Override
    public void onBackPressed() {
        if (navegador.canGoBack()) {
            navegador.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public class InterfazComunicacion {
        Context mContext;

        InterfazComunicacion(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void volver() {
            finish();
        }
    }

    // conectividad
    private boolean comprobarConectividad() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if ((info == null || !info.isConnected() || !info.isAvailable())) {
            Toast.makeText(EventosWeb.this, "Oops! No tienes conexión a internet", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do nothing
                } else {
                    Toast.makeText(EventosWeb.this, "Permiso denegado para conocer el estado de la red.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eventos_web, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View vista = (View) findViewById(android.R.id.content);
        Bundle bundle = new Bundle();
        int id = item.getItemId();
        switch (id) {
            case R.id.show_web:
                navegador.loadUrl("https://us-central1-eventos-98475.cloudfunctions.net/mostrarEventosHtml?evento=" + evento);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

