package com.contactos.gestiondecontactos;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/** Pantalla temporal para implementar el listado en el siguiente paso. */
public class ActivityLista extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView aviso = new TextView(this);
        aviso.setText("Contactos\nEl listado se implementará en el siguiente paso."); //por si se prueba antes del lunes
        aviso.setGravity(android.view.Gravity.CENTER);
        aviso.setTextColor(getColor(R.color.contact_text));
        aviso.setBackgroundColor(getColor(R.color.contact_background));
        setContentView(aviso);
    }
}
