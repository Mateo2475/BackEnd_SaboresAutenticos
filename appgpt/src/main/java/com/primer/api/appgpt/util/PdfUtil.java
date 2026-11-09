package com.primer.api.appgpt.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Generador mínimo de PDF sin dependencias externas.
 * Sirve para exportar reportes simples (RF14 / RF15 / RF23).
 * No pretende reemplazar a una librería como OpenPDF/iText.
 */
public final class PdfUtil {

    private static final int LINEAS_POR_PAGINA = 45;

    private PdfUtil() {
    }

    public static byte[] generarTicket(String titulo, List<String> lineas) {
        return generarConAncho(titulo, lineas, 226, 36);
    }

    public static byte[] generar(String titulo, List<String> lineas) {
        return generarConAncho(titulo, lineas, 595, 45);
    }

    private static byte[] generarConAncho(String titulo, List<String> lineas, int ancho, int lineasPorPagina) {
        List<String> contenido = new ArrayList<>();
        contenido.add("=== SABORES AUTENTICOS ===");
        contenido.add(titulo);
        contenido.add(" ");
        contenido.addAll(lineas);

        int totalPaginas = Math.max(1, (int) Math.ceil(contenido.size() / (double) lineasPorPagina));

        List<String> objetos = new ArrayList<>();
        // 1: catálogo, 2: páginas, 3: fuente
        objetos.add("<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < totalPaginas; i++) {
            kids.append(4 + i * 2).append(" 0 R ");
        }
        objetos.add("<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + totalPaginas + " >>");
        objetos.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        for (int pagina = 0; pagina < totalPaginas; pagina++) {
            int desde = pagina * lineasPorPagina;
            int hasta = Math.min(contenido.size(), desde + lineasPorPagina);

            StringBuilder stream = new StringBuilder();
            stream.append("BT\n/F1 10 Tf\n40 800 Td\n14 TL\n");
            for (int i = desde; i < hasta; i++) {
                stream.append("(").append(escapar(contenido.get(i))).append(") Tj T*\n");
            }
            stream.append("ET\n");

            objetos.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + ancho + " 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + (5 + pagina * 2) + " 0 R >>");
            objetos.add("<< /Length " + stream.toString().getBytes(StandardCharsets.ISO_8859_1).length
                    + " >>\nstream\n" + stream + "endstream");
        }

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        int[] offsets = new int[objetos.size() + 1];
        for (int i = 0; i < objetos.size(); i++) {
            offsets[i + 1] = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
            pdf.append(i + 1).append(" 0 obj\n").append(objetos.get(i)).append("\nendobj\n");
        }

        int xrefOffset = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
        pdf.append("xref\n0 ").append(objetos.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int i = 1; i <= objetos.size(); i++) {
            pdf.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        pdf.append("trailer\n<< /Size ").append(objetos.size() + 1)
                .append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefOffset).append("\n%%EOF");

        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.replace("\r", " ").replace("\n", " ");
        limpio = limpio.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        // Evita caracteres fuera de Latin-1 que rompan el PDF
        StringBuilder sb = new StringBuilder();
        for (char c : limpio.toCharArray()) {
            sb.append(c <= 255 ? c : '?');
        }
        return sb.toString();
    }
}
