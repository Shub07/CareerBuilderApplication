package com.org.careerbuilder.service.support;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.org.careerbuilder.dto.response.AdminExamDtos;

import java.io.ByteArrayOutputStream;
import java.util.List;

final class AdminExamResultsPdfWriter {

    private AdminExamResultsPdfWriter() {
    }

    static byte[] write(String examTitle, String format, List<AdminExamDtos.FinalResultRow> rows) {
        boolean detailed = "DETAILED".equalsIgnoreCase(format);
        try {
            Document doc = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(examTitle + " — Results", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            doc.add(new Paragraph(detailed ? "Detailed report" : "Summary report",
                    FontFactory.getFont(FontFactory.HELVETICA, 10)));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(detailed ? 6 : 5);
            table.setWidthPercentage(100);
            table.addCell(header("Student"));
            table.addCell(header("Total Marks"));
            table.addCell(header("Grade"));
            table.addCell(header("Rank"));
            table.addCell(header("Status"));
            if (detailed) {
                table.addCell(header("Student ID"));
            }
            for (AdminExamDtos.FinalResultRow row : rows) {
                table.addCell(cell(row.studentName()));
                table.addCell(cell(row.totalMarksLabel()));
                table.addCell(cell(row.grade()));
                table.addCell(cell("#" + row.rank()));
                table.addCell(cell(row.statusLabel()));
                if (detailed) {
                    table.addCell(cell(String.valueOf(row.studentId())));
                }
            }
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate results PDF", e);
        }
    }

    private static PdfPCell header(String text) {
        return new PdfPCell(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
    }

    private static PdfPCell cell(String text) {
        return new PdfPCell(new Paragraph(text != null ? text : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
    }
}
