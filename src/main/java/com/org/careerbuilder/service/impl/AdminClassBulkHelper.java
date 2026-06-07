package com.org.careerbuilder.service.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel/CSV template generation and parsing for the class "Bulk Upload Students" flow.
 * Columns: Registration Number, Student Name, Section.
 */
final class AdminClassBulkHelper {

    static final List<String> HEADERS = List.of("Registration Number", "Student Name", "Section");

    private AdminClassBulkHelper() {
    }

    record ParsedRow(String regNumber, String name, String section) {
    }

    static byte[] template() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                header.createCell(i).setCellValue(HEADERS.get(i));
            }
            String[][] samples = {
                    {"ADM-001", "Sample Student 1", "A"},
                    {"ADM-002", "Sample Student 2", "A"},
                    {"ADM-003", "Sample Student 3", "A"}
            };
            for (int r = 0; r < samples.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < samples[r].length; c++) {
                    row.createCell(c).setCellValue(samples[r][c]);
                }
            }
            for (int i = 0; i < HEADERS.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build template", e);
        }
    }

    static List<ParsedRow> parse(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        try {
            if (filename.endsWith(".csv")) {
                return parseCsv(file);
            }
            return parseWorkbook(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read the uploaded file: " + e.getMessage());
        }
    }

    private static List<ParsedRow> parseWorkbook(MultipartFile file) throws Exception {
        List<ParsedRow> rows = new ArrayList<>();
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean first = true;
            for (Row row : sheet) {
                if (first) {
                    first = false;
                    continue; // skip header
                }
                String reg = cellString(row.getCell(0));
                String name = cellString(row.getCell(1));
                String section = cellString(row.getCell(2));
                if (reg.isBlank() && name.isBlank() && section.isBlank()) {
                    continue;
                }
                rows.add(new ParsedRow(reg, name, section));
            }
        }
        return rows;
    }

    private static List<ParsedRow> parseCsv(MultipartFile file) throws Exception {
        List<ParsedRow> rows = new ArrayList<>();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = content.split("\\r?\\n");
        for (int i = 1; i < lines.length; i++) { // skip header
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] cols = line.split(",", -1);
            String reg = cols.length > 0 ? cols[0].trim() : "";
            String name = cols.length > 1 ? cols[1].trim() : "";
            String section = cols.length > 2 ? cols[2].trim() : "";
            rows.add(new ParsedRow(reg, name, section));
        }
        return rows;
    }

    private static String cellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
