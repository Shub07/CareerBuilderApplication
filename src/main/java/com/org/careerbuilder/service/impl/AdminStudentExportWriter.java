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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AdminStudentExportWriter {

    private AdminStudentExportWriter() {
    }

    static byte[] writeExcel(List<String> headers, List<Map<String, String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");
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

    static byte[] writeCsv(List<String> headers, List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", headers.stream().map(AdminStudentExportWriter::csvEscape).toList())).append("\n");
        for (Map<String, String> data : rows) {
            List<String> line = new ArrayList<>();
            for (String h : headers) {
                line.add(csvEscape(data.getOrDefault(h, "")));
            }
            sb.append(String.join(",", line)).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    static byte[] writePdf(List<String> headers, List<Map<String, String>> rows) {
        try {
            Document doc = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, baos);
            doc.open();
            doc.add(new Paragraph("Students Export", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
                table.addCell(cell);
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

    static Map<String, String> fieldLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("studentName", "Student Name");
        labels.put("registrationNumber", "Registration Number");
        labels.put("rollNumber", "Roll Number");
        labels.put("gender", "Gender");
        labels.put("dob", "DOB");
        labels.put("phone", "Phone");
        labels.put("email", "Email");
        labels.put("address", "Address");
        labels.put("admissionDate", "Admission Date");
        labels.put("fatherName", "Father Name");
        labels.put("motherName", "Mother Name");
        labels.put("parentPhone", "Parent Phone");
        labels.put("parentEmail", "Parent Email");
        labels.put("occupation", "Occupation");
        labels.put("parentAddress", "Parent Address");
        labels.put("className", "Class");
        labels.put("section", "Section");
        labels.put("academicYear", "Academic Year");
        labels.put("feeStatus", "Fee Status");
        labels.put("status", "Status");
        return labels;
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "\"\"";
        }
        String v = value.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }
}
