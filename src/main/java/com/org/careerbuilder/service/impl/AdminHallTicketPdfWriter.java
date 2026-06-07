package com.org.careerbuilder.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.org.careerbuilder.dto.response.AdminExamDtos;

import java.io.ByteArrayOutputStream;
import java.util.List;

final class AdminHallTicketPdfWriter {

    private AdminHallTicketPdfWriter() {
    }

    static byte[] writeSingle(AdminExamDtos.HallTicketPreviewResponse ticket) {
        return writeBatch(List.of(ticket));
    }

    static byte[] writeBatch(List<AdminExamDtos.HallTicketPreviewResponse> tickets) {
        try {
            Document doc = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();
            for (int i = 0; i < tickets.size(); i++) {
                if (i > 0) {
                    doc.newPage();
                }
                appendTicket(doc, tickets.get(i));
            }
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate hall ticket PDF", e);
        }
    }

    private static void appendTicket(Document doc, AdminExamDtos.HallTicketPreviewResponse t) throws Exception {
        doc.add(new Paragraph(t.schoolName().toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        doc.add(new Paragraph("OFFICIAL EXAMINATION AUTHORITY", FontFactory.getFont(FontFactory.HELVETICA, 9)));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("HALL TICKET: " + t.examTitle(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Student: " + t.studentName(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        doc.add(new Paragraph("Hall Ticket No: " + t.hallTicketNumber(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        doc.add(new Paragraph("Admission ID: " + t.admissionId(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        doc.add(new Paragraph("Class: " + t.classSection(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        doc.add(new Paragraph(" "));

        if (!t.schedule().isEmpty()) {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell(headerCell("Subject"));
            table.addCell(headerCell("Date"));
            table.addCell(headerCell("Timing"));
            table.addCell(headerCell("Venue"));
            for (AdminExamDtos.HallTicketScheduleLine line : t.schedule()) {
                table.addCell(bodyCell(line.subject()));
                table.addCell(bodyCell(line.date()));
                table.addCell(bodyCell(line.timing()));
                table.addCell(bodyCell(line.venue()));
            }
            doc.add(table);
        }

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Instructions:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        for (String instruction : t.instructions()) {
            doc.add(new Paragraph("• " + instruction, FontFactory.getFont(FontFactory.HELVETICA, 8)));
        }
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Document Hash: " + t.documentHash(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
        if (t.verified()) {
            doc.add(new Paragraph("VERIFIED DIGITAL CERTIFICATE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
        }
    }

    private static PdfPCell headerCell(String text) {
        return new PdfPCell(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
    }

    private static PdfPCell bodyCell(String text) {
        return new PdfPCell(new Paragraph(text != null ? text : "", FontFactory.getFont(FontFactory.HELVETICA, 8)));
    }
}
