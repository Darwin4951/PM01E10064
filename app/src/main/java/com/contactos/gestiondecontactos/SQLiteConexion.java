package com.contactos.gestiondecontactos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class SQLiteConexion extends SQLiteOpenHelper {
    public SQLiteConexion(Context context) { super(context, "DBContactos", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contactos (id INTEGER PRIMARY KEY AUTOINCREMENT, pais TEXT, nombre TEXT, telefono TEXT, nota TEXT, imagen TEXT)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Agregar migraciones que conserven los contactos al incrementar la versión.
    }
    private ContentValues valores(Contactos c) {
        ContentValues v = new ContentValues();
        v.put("pais", c.getPais()); v.put("nombre", c.getNombre());
        v.put("telefono", c.getTelefono()); v.put("nota", c.getNota()); v.put("imagen", c.getImagen());
        return v;
    }
    public long insertarContacto(Contactos c) {
        return getWritableDatabase().insert("contactos", null, valores(c));
    }
    public int actualizarContacto(Contactos c) {
        if (c.getId() == null) return 0;
        return getWritableDatabase().update("contactos", valores(c), "id = ?", new String[]{c.getId().toString()});
    }
    public ArrayList<Contactos> obtenerContactos() {
        ArrayList<Contactos> contactos = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("contactos", new String[]{"id", "pais", "nombre", "telefono", "nota", "imagen"}, null, null, null, null, "nombre COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) contactos.add(new Contactos(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
        }
        return contactos;
    }
    public Contactos obtenerContacto(int id) {
        try (Cursor c = getReadableDatabase().query("contactos", new String[]{"id", "pais", "nombre", "telefono", "nota", "imagen"}, "id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return new Contactos(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5));
            }
            return null;
        }
    }
    public int eliminarContacto(int id) {
        return getWritableDatabase().delete("contactos", "id = ?", new String[]{String.valueOf(id)});
    }
}
