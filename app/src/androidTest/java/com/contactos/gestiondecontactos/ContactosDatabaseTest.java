package com.contactos.gestiondecontactos;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.DatabaseErrorHandler;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ContactosDatabaseTest {
    @Test public void consultaActualizaConservaImagenYEliminaPorId() {
        Context real = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Base exclusiva de pruebas: nunca abrir ni borrar DBContactos del usuario.
        String nombre = "prueba_pantallas_" + System.nanoTime();
        Context aislado = new ContextWrapper(real) {
            @Override public SQLiteDatabase openOrCreateDatabase(String n, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler handler) {
                return real.openOrCreateDatabase(nombre, mode, factory, handler);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String n, int mode, SQLiteDatabase.CursorFactory factory) {
                return real.openOrCreateDatabase(nombre, mode, factory);
            }
            @Override public boolean deleteDatabase(String n) { return real.deleteDatabase(nombre); }
        };
        try (SQLiteConexion db = new SQLiteConexion(aislado)) {
            Bitmap foto = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            foto.eraseColor(android.graphics.Color.BLUE);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            foto.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String imagen = Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP);
            int id = (int) db.insertarContacto(new Contactos(null, "Honduras (+504)", "Ana", "98765432", "Nota", imagen));
            int otro = (int) db.insertarContacto(new Contactos(null, "Honduras (+504)", "Luis", "87654321", "Otra", ""));
            assertTrue(id > 0);
            assertEquals(2, db.obtenerContactos().size());
            Contactos contacto = db.obtenerContacto(id);
            assertEquals(imagen, contacto.getImagen());
            byte[] datos = Base64.decode(contacto.getImagen(), Base64.DEFAULT);
            assertNotNull(BitmapFactory.decodeByteArray(datos, 0, datos.length));
            contacto.setNombre("Ana Maria"); contacto.setTelefono("99998888");
            assertEquals(1, db.actualizarContacto(contacto));
            assertEquals(2, db.obtenerContactos().size());
            assertEquals("Ana Maria", db.obtenerContacto(id).getNombre());
            assertEquals(imagen, db.obtenerContacto(id).getImagen());
            assertEquals(1, db.eliminarContacto(id));
            assertNull(db.obtenerContacto(id));
            assertEquals("Luis", db.obtenerContacto(otro).getNombre());
        } finally { aislado.deleteDatabase("DBContactos"); }
    }
}
