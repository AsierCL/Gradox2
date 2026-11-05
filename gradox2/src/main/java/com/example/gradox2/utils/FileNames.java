package com.example.gradox2.utils;

import java.util.regex.Pattern;

/**
 * Normaliza el nombre de archivo que envía el cliente antes de persistirlo o de
 * usarlo en una cabecera {@code Content-Disposition}.
 *
 * <p>El nombre lo controla el cliente: puede traer componentes de ruta
 * ({@code ../../notas.pdf}, {@code C:\Users\x\notas.pdf}), caracteres de control
 * o una longitud mayor que la columna {@code varchar(255)} donde se guarda.
 * Cualquiera de esos casos terminaba en un error de integridad al subir.
 */
public final class FileNames {

    /** Longitud de {@code files.original_filename} y {@code temp_files.original_filename}. */
    public static final int MAX_LENGTH = 255;

    /** Extensión máxima que se preserva al truncar; más larga se considera parte del nombre. */
    private static final int MAX_EXTENSION_LENGTH = 20;

    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cntrl}");

    private FileNames() {
    }

    /**
     * @return el nombre sin rutas ni caracteres de control, truncado a
     *         {@value #MAX_LENGTH} caracteres conservando la extensión, o
     *         {@code null} si no queda nada utilizable.
     */
    public static String sanitize(String rawFilename) {
        if (rawFilename == null) {
            return null;
        }

        String name = rawFilename;
        int lastSeparator = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            name = name.substring(lastSeparator + 1);
        }

        name = CONTROL_CHARACTERS.matcher(name).replaceAll("").trim();
        if (name.isEmpty()) {
            return null;
        }

        return name.length() <= MAX_LENGTH ? name : truncate(name);
    }

    private static String truncate(String name) {
        int lastDot = name.lastIndexOf('.');
        boolean hasExtension = lastDot > 0 && name.length() - lastDot <= MAX_EXTENSION_LENGTH;
        String extension = hasExtension ? name.substring(lastDot) : "";
        return name.substring(0, MAX_LENGTH - extension.length()) + extension;
    }
}
