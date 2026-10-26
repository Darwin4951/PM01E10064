package com.contactos.gestiondecontactos;


final class NumeroTelefonico {
    private NumeroTelefonico() { }
    static String completo(String pais, String telefono) {
        if (telefono == null) return "";
        String original = telefono.trim();
        String digitos = original.replaceAll("[^0-9]", "");
        if (digitos.isEmpty()) return "";
        if (original.startsWith("+")) return "+" + digitos;
        if (original.startsWith("00")) return "+" + digitos.substring(2);
        if (pais == null) return "";
        int inicioCodigo = pais.indexOf('+');
        if (inicioCodigo == -1) return "";
        String codigo = "";
        for (int i = inicioCodigo + 1; i < pais.length(); i++) {
            char caracter = pais.charAt(i);
            if (caracter < '0' || caracter > '9') break;
            codigo += caracter;
        }
        if (codigo.isEmpty()) return "";
        // Los países del catálogo tienen números nacionales de 8 dígitos.
        if (digitos.startsWith(codigo) && digitos.length() > 8) return "+" + digitos;
        return "+" + codigo + digitos;
    }
}
