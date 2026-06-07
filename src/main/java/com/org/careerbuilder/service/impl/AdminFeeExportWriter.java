package com.org.careerbuilder.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders a fee report (headers + rows) to Excel / CSV / PDF.
 */
final class AdminFeeExportWriter {

    private AdminFeeExportWriter() {
    }

    static byte[] write(String format, String title, List<String> headers, List<Map<String, String>> rows) {
        return switch (format == null ? "EXCEL" : format.toUpperCase()) {
            case "CSV" -> writeCsv(headers, rows);
            case "PDF" -> writePdf(title, headers, rows);
            default -> writeExcel(title, headers, rows);
        };
    }

    private static byte[] writeExcel(String title, List<String> headers, List<Map<String, String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(safeSheetName(title));
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
            }
            int r = 1;
            for (Map<String, String> data : rows) {
                Row row = sheet.createRow(r++);
                for (int i = 0; i < headers.size(); i++) {
                    row.createCell(i).setCellValue(data.getOrDefault(headers.get(i), ""));
                }
            }
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to export Excel", e);
        }
    }

    private static byte[] writeCsv(List<String> headers, List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers.stream().map(AdminFeeExportWriter::csvEscape).toList())).append("\n");
        for (Map<String, String> data : rows) {
            List<String> line = new ArrayList<>();
            for (String h : headers) {
                line.add(csvEscape(data.getOrDefault(h, "")));
            }
            sb.append(String.join(",", line)).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] writePdf(String title, List<String> headers, List<Map<String, String>> rows) {
        try {
            Document doc = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, baos);
            doc.open();
            doc.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            for (String h : headers) {
                table.addCell(new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8))));
            }
            for (Map<String, String> data : rows) {
                for (String h : headers) {
                    table.addCell(new PdfPCell(new Paragraph(data.getOrDefault(h, ""),
                            FontFactory.getFont(FontFactory.HELVETICA, 8))));
                }
            }
            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to export PDF", e);
        }
    }

    private static String safeSheetName(String title) {
        if (title == null || title.isBlank()) {
            return "report";
        }
        String cleaned = title.replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
