package com.contactos.gestiondecontactos;

import android.Manifest;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;

public class ActivityLista extends AppCompatActivity {
    private SQLiteConexion conexion;
    private final ArrayList<Contactos> contactos = new ArrayList<>();
    private BaseAdapter adaptador;
    private String numeroPendiente;
    private int idPendiente = -1;
    private ActivityResultLauncher<String> permiso;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            numeroPendiente = state.getString("numero_pendiente");
            idPendiente = state.getInt("id_pendiente", -1);
        }
        permiso = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                concedido -> responderPermiso(concedido));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listaRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        conexion = new SQLiteConexion(this);
        findViewById(R.id.btnAgregar).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        ListView lista = findViewById(R.id.listaContactos);
        lista.setEmptyView(findViewById(R.id.listaVacia));
        adaptador = new BaseAdapter() {
            @Override public int getCount() { return contactos.size(); }
            @Override public Contactos getItem(int position) { return contactos.get(position); }
            @Override public long getItemId(int position) { return getItem(position).getId(); }
            @Override public View getView(int position, View view, ViewGroup parent) {
                if (view == null) view = getLayoutInflater().inflate(R.layout.item_contacto, parent, false);
                Contactos contacto = getItem(position);
                ((TextView) view.findViewById(R.id.nombreContacto)).setText(contacto.getNombre());
                ((TextView) view.findViewById(R.id.paisContacto)).setText(contacto.getPais());
                ((TextView) view.findViewById(R.id.telefonoContacto)).setText(numero(contacto));
                ImageView foto = view.findViewById(R.id.fotoContacto);
                Bitmap bitmap = imagen(contacto.getImagen(), 128);
                foto.setImageResource(R.drawable.ic_camera);
                if (bitmap == null) {
                    foto.setScaleType(ImageView.ScaleType.CENTER);
                } else {
                    foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    foto.setImageBitmap(bitmap);
                }
                foto.setContentDescription("Ver imagen de " + contacto.getNombre());
                foto.setOnClickListener(v -> verImagen(contacto));
                view.findViewById(R.id.llamarContacto).setOnClickListener(v -> confirmarLlamada(contacto));
                view.findViewById(R.id.editarContacto).setOnClickListener(v -> editarContacto(contacto));
                view.findViewById(R.id.eliminarContacto).setOnClickListener(v -> eliminar(contacto));
                view.findViewById(R.id.compartirContacto).setOnClickListener(v -> compartir(contacto));
                view.findViewById(R.id.verImagenContacto).setOnClickListener(v -> verImagen(contacto));
                return view;
            }
        };
        lista.setAdapter(adaptador);
    }
    // Se usa el contacto confirmado, aunque la lista cambie durante la solicitud.
    private void responderPermiso(boolean concedido) {
        String numero = numeroPendiente;
        int id = idPendiente;
        numeroPendiente = null;
        idPendiente = -1;
        if (!concedido) { mensaje("Permiso de llamada denegado"); return; }
        try {
            Contactos actual = conexion.obtenerContacto(id);
            if (actual != null && numero != null && numero.equals(numero(actual))) llamar(numero);
            else mensaje("El contacto cambió. Confirme la llamada nuevamente");
        } catch (SQLiteException e) { mensaje("No se pudo consultar el contacto"); }
    }

    private void editarContacto(Contactos contacto) {
        Intent pantalla = new Intent(this, MainActivity.class);
        pantalla.putExtra("modo_editar", true);
        pantalla.putExtra("id", contacto.getId().intValue());
        startActivity(pantalla);
    }

    @Override protected void onResume() { super.onResume(); refrescar(); }
    private void refrescar() {
        try {
            ArrayList<Contactos> actuales = conexion.obtenerContactos();
            contactos.clear();
            contactos.addAll(actuales);
            adaptador.notifyDataSetChanged();
        } catch (SQLiteException e) { mensaje("No se pudieron cargar los contactos"); }
    }
    private String numero(Contactos contacto) { return NumeroTelefonico.completo(contacto.getPais(), contacto.getTelefono()); }
    private void eliminar(Contactos contacto) {
        new MaterialAlertDialogBuilder(this).setTitle("Eliminar contacto")
                .setMessage("¿Desea eliminar a " + contacto.getNombre() + "?")
                .setNegativeButton("Cancelar", null).setPositiveButton("Eliminar", (d, w) -> {
                    try {
                        int filas = conexion.eliminarContacto(contacto.getId());
                        refrescar();
                        if (filas > 0) {
                            mensaje("Contacto eliminado");
                        } else {
                            mensaje("El contacto ya no existe");
                        }
                    } catch (SQLiteException e) { mensaje("No se pudo eliminar el contacto"); }
                }).show();
    }
    private void compartir(Contactos contacto) {
        Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, contacto.getNombre() + "\n" + numero(contacto));
        try { startActivity(Intent.createChooser(intent, "Compartir contacto")); }
        catch (ActivityNotFoundException | SecurityException e) { mensaje("No hay aplicaciones disponibles para compartir"); }
    }
    private static Bitmap imagen(String valor, int limite) {
        if (valor == null || valor.isEmpty()) return null;
        try {
            byte[] bytes = Base64.decode(valor, Base64.DEFAULT);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            options.inSampleSize = 1;
            while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > limite) options.inSampleSize *= 2;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (IllegalArgumentException e) { return null; }
    }
    private void verImagen(Contactos contacto) {
        Bitmap bitmap = imagen(contacto.getImagen(), 1024);
        if (bitmap == null) { mensaje("El contacto no tiene una imagen disponible"); return; }
        View vista = getLayoutInflater().inflate(R.layout.dialog_imagen, null);
        ((ImageView) vista.findViewById(R.id.imagenAmpliada)).setImageBitmap(bitmap);
        new MaterialAlertDialogBuilder(this).setTitle(contacto.getNombre()).setView(vista).setPositiveButton("Cerrar", null).show();
    }
    private void confirmarLlamada(Contactos contacto) {
        String numero = numero(contacto);
        if (numero.isEmpty()) { mensaje("El contacto no tiene un número válido"); return; }
        if (numeroPendiente != null) { mensaje("Hay una solicitud de permiso pendiente"); return; }
        new MaterialAlertDialogBuilder(this).setTitle("Llamar")
                .setMessage("¿Desea llamar a " + contacto.getNombre() + " al número " + numero + "?")
                .setNegativeButton("No", null).setPositiveButton("Sí", (d, w) -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) llamar(numero);
                    else {
                        numeroPendiente = numero; idPendiente = contacto.getId();
                        permiso.launch(Manifest.permission.CALL_PHONE);
                    }
                }).show();
    }
    private void llamar(String numero) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) return;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + numero));
        try {
            if (intent.resolveActivity(getPackageManager()) == null) { mensaje("No hay una aplicación disponible para realizar llamadas"); return; }
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) { mensaje("No se pudo iniciar la llamada"); }
    }
    private void mensaje(String texto) { Toast.makeText(this, texto, Toast.LENGTH_LONG).show(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("numero_pendiente", numeroPendiente); state.putInt("id_pendiente", idPendiente);
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() { if (conexion != null) conexion.close(); super.onDestroy(); }
}
