package com.org.careerbuilder.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AdminExamExportWriter {

    private AdminExamExportWriter() {
    }

    static byte[] write(String format, List<String> headers, List<Map<String, String>> rows) {
        String f = format == null ? "EXCEL" : format.trim().toUpperCase();
        return switch (f) {
            case "CSV" -> writeCsv(headers, rows);
            case "PDF" -> AdminStudentExportWriter.writePdf(headers, rows);
            default -> AdminStudentExportWriter.writeExcel(headers, rows);
        };
    }

    static String contentType(String format) {
        String f = format == null ? "EXCEL" : format.trim().toUpperCase();
        return switch (f) {
            case "CSV" -> "text/csv";
            case "PDF" -> "application/pdf";
            default -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        };
    }

    static String extension(String format) {
        String f = format == null ? "EXCEL" : format.trim().toUpperCase();
        return switch (f) {
            case "CSV" -> "csv";
            case "PDF" -> "pdf";
            default -> "xlsx";
        };
    }

    private static byte[] writeCsv(List<String> headers, List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers.stream().map(AdminExamExportWriter::csvEscape).toList())).append("\n");
        for (Map<String, String> data : rows) {
            List<String> line = new ArrayList<>();
            for (String h : headers) {
                line.add(csvEscape(data.getOrDefault(h, "")));
            }
            sb.append(String.join(",", line)).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
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

    static Map<String, String> row(LinkedHashMap<String, String> map) {
        return map;
    }
}
