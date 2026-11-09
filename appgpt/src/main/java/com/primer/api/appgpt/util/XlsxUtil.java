package com.primer.api.appgpt.util;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generador mínimo de XLSX (Office Open XML) sin dependencias externas.
 * Permite exportar reportes tabulares compatibles con Excel/LibreOffice.
 */
public final class XlsxUtil {

    private XlsxUtil() {
    }

    public static byte[] generar(String hoja, List<String> encabezados, List<List<String>> filas) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // [Content_Types].xml
            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "</Types>").getBytes("UTF-8"));
            zos.closeEntry();

            // _rels/.rels
            zos.putNextEntry(new ZipEntry("_rels/.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>").getBytes("UTF-8"));
            zos.closeEntry();

            // xl/workbook.xml
            zos.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"" + escaparXml(hoja) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                    + "</workbook>").getBytes("UTF-8"));
            zos.closeEntry();

            // xl/_rels/workbook.xml.rels
            zos.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "</Relationships>").getBytes("UTF-8"));
            zos.closeEntry();

            // xl/worksheets/sheet1.xml
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
            int fila = 1;
            sb.append("<row r=\"").append(fila).append("\">");
            for (int i = 0; i < encabezados.size(); i++) {
                sb.append(celda(i + 1, fila, encabezados.get(i)));
            }
            sb.append("</row>");
            fila++;
            for (List<String> r : filas) {
                sb.append("<row r=\"").append(fila).append("\">");
                for (int i = 0; i < r.size(); i++) {
                    sb.append(celda(i + 1, fila, r.get(i)));
                }
                sb.append("</row>");
                fila++;
            }
            sb.append("</sheetData></worksheet>");

            zos.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            zos.write(sb.toString().getBytes("UTF-8"));
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el XLSX: " + e.getMessage(), e);
        }
    }

    private static String celda(int col, int fila, String valor) {
        String ref = columna(col) + fila;
        String v = escaparXml(valor == null ? "" : valor);
        return "<c r=\"" + ref + "\" t=\"inlineStr\"><is><t>" + v + "</t></is></c>";
    }

    private static String columna(int n) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            int r = (n - 1) % 26;
            sb.insert(0, (char) ('A' + r));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    private static String escaparXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}