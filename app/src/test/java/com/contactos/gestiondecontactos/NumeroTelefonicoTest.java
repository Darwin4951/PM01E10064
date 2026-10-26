package com.contactos.gestiondecontactos;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class NumeroTelefonicoTest {
    @Test public void agregaCodigoAlNumeroLocal() {
        assertEquals("+50498765432", NumeroTelefonico.completo("Honduras (+504)", "9876 5432"));
        assertEquals("+50761234567", NumeroTelefonico.completo("Panama (+507)", "61234567"));
    }
    @Test public void noDuplicaPrefijos() {
        for (String numero : new String[]{"50498765432", "+50498765432", "0050498765432"})
            assertEquals("+50498765432", NumeroTelefonico.completo("Honduras (+504)", numero));
        assertEquals("+50688887777", NumeroTelefonico.completo("Honduras (+504)", "+50688887777"));
    }
    @Test public void conservaInicioLocalSimilarAlCodigo() {
        assertEquals("+50450412345", NumeroTelefonico.completo("Honduras (+504)", "50412345"));
    }
    @Test public void rechazaDatosVacios() {
        assertEquals("", NumeroTelefonico.completo(null, null));
        assertEquals("", NumeroTelefonico.completo("Honduras (+504)", ""));
        assertEquals("", NumeroTelefonico.completo("", "98765432"));
    }
}
