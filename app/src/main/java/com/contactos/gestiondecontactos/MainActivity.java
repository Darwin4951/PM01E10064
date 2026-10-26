package com.contactos.gestiondecontactos;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private EditText etNombre, etTelefono, etNota;
    private Spinner spPais;
    private ImageView ivImagen;
    private SQLiteConexion conexion;
    private String imagen = "";
    private boolean modoEditar;
    private int contactoId = -1;
    private ActivityResultLauncher<String> galeria;
    private ActivityResultLauncher<Void> camara;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) cargarGaleria(uri);
        });
        // La aplicación de cámara entrega una miniatura: evita fotos grandes en SQLite e Intents.
        // Al delegar la captura a otra app no se necesita el permiso CAMERA.
        camara = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
            if (bitmap != null) establecerImagen(bitmap);
        });
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        conexion = new SQLiteConexion(this);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        spPais = findViewById(R.id.spPais);
        ivImagen = findViewById(R.id.ivImagen);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.paises, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPais.setAdapter(adapter);
        modoEditar = getIntent().getBooleanExtra("modo_editar", false);
        contactoId = getIntent().getIntExtra("id", -1);
        if (modoEditar) {
            ((MaterialToolbar) findViewById(R.id.toolbar)).setTitle(R.string.actualizar_contacto);
            ((MaterialButton) findViewById(R.id.btnGuardar)).setText(R.string.actualizar_contacto);
            if (contactoId <= 0) {
                Toast.makeText(this, "El contacto no tiene un identificador válido", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Contactos actual;
            try { actual = conexion.obtenerContacto(contactoId); }
            catch (SQLiteException e) { actual = null; }
            if (actual == null) {
                Toast.makeText(this, "No se pudo cargar el contacto", Toast.LENGTH_LONG).show();
                finish(); return;
            }
            etNombre.setText(actual.getNombre());
            etTelefono.setText(actual.getTelefono());
            etNota.setText(actual.getNota());
            String pais = actual.getPais();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(pais)) spPais.setSelection(i);
            }
            imagen = actual.getImagen();
        }
        if (savedInstanceState != null) imagen = savedInstanceState.getString("imagen_formulario", "");
        mostrarImagen();
        ivImagen.setOnClickListener(v -> elegirImagen());
        findViewById(R.id.tvAgregarImagen).setOnClickListener(v -> elegirImagen());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        findViewById(R.id.btnVerContactos).setOnClickListener(v -> startActivity(new Intent(this, ActivityLista.class)));
    }

    private void elegirImagen() {
        new MaterialAlertDialogBuilder(this).setTitle("Agregar imagen")
                .setItems(new String[]{"Galería", "Tomar foto"}, (dialog, which) -> {
                    try {
                        if (which == 0) galeria.launch("image/*");
                        else camara.launch(null);
                    } catch (ActivityNotFoundException | SecurityException e) {
                        alerta("No hay una aplicación disponible para esta opción");
                    }
                }).setNegativeButton("Cancelar", null).show();
    }

    private void cargarGaleria(Uri uri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream stream = getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(stream, null, options);
            }
            options.inSampleSize = 1;
            while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > 1024) options.inSampleSize *= 2;
            options.inJustDecodeBounds = false;
            try (InputStream stream = getContentResolver().openInputStream(uri)) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
                if (bitmap == null) alerta("No se pudo leer la imagen seleccionada");
                else establecerImagen(bitmap);
            }
        } catch (IOException | SecurityException e) {
            alerta("No se pudo abrir la imagen seleccionada");
        }
    }

    private void establecerImagen(Bitmap bitmap) {
        float escala = Math.min(1f, 512f / Math.max(bitmap.getWidth(), bitmap.getHeight()));
        Bitmap reducida = Bitmap.createScaledBitmap(bitmap, Math.max(1, Math.round(bitmap.getWidth() * escala)), Math.max(1, Math.round(bitmap.getHeight() * escala)), true);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        reducida.compress(Bitmap.CompressFormat.JPEG, 70, buffer);
        imagen = Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);
        ivImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivImagen.setImageBitmap(reducida);
    }

    private void mostrarImagen() {
        ivImagen.setScaleType(ImageView.ScaleType.CENTER);
        ivImagen.setImageResource(R.drawable.ic_camera);
        if (imagen == null || imagen.isEmpty()) { imagen = ""; return; }
        try {
            byte[] bytes = Base64.decode(imagen, Base64.DEFAULT);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            bounds.inSampleSize = 1;
            while (Math.max(bounds.outWidth, bounds.outHeight) / bounds.inSampleSize > 1024) bounds.inSampleSize *= 2;
            bounds.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
            if (bitmap != null) {
                ivImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivImagen.setImageBitmap(bitmap);
            }
        } catch (IllegalArgumentException e) { /* Conservar el valor almacenado. */ }
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String nota = etNota.getText().toString().trim();
        if (nombre.isEmpty()) { alerta("Debe escribir un nombre"); etNombre.requestFocus(); return; }
        if (telefono.isEmpty()) { alerta("Debe escribir un teléfono"); etTelefono.requestFocus(); return; }
        if (nota.isEmpty()) { alerta("Debe escribir una nota"); etNota.requestFocus(); return; }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            alerta("El nombre solo acepta letras y espacios"); etNombre.requestFocus(); return;
        }
        if (!telefono.matches("^[0-9]+$")) {
            alerta("El teléfono solo acepta números"); etTelefono.requestFocus(); return;
        }
        Contactos contacto = new Contactos(modoEditar ? contactoId : null, spPais.getSelectedItem().toString(), nombre, telefono, nota, imagen);
        try {
            boolean exito;
            // Editar modifica la fila por ID; registrar crea una fila nueva.
            if (modoEditar) {
                exito = conexion.actualizarContacto(contacto) > 0;
            } else {
                exito = conexion.insertarContacto(contacto) != -1;
            }
            if (!exito) { alerta("No se pudo guardar el contacto"); return; }
            Toast.makeText(this, modoEditar ? "Contacto actualizado correctamente" : "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
            if (modoEditar) { setResult(RESULT_OK); finish(); }
            else {
                etNombre.setText(""); etTelefono.setText(""); etNota.setText("");
                spPais.setSelection(0); imagen = ""; mostrarImagen(); etNombre.requestFocus();
            }
        } catch (SQLiteException e) { alerta("No se pudo guardar el contacto en la base de datos"); }
    }

    private void alerta(String mensaje) {
        new MaterialAlertDialogBuilder(this).setTitle("Gestión de contactos").setMessage(mensaje).setPositiveButton("Aceptar", null).show();
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putString("imagen_formulario", imagen);
        super.onSaveInstanceState(outState);
    }
    @Override protected void onDestroy() {
        if (conexion != null) conexion.close();
        super.onDestroy();
    }
}
