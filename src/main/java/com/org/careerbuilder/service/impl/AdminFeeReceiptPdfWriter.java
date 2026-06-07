package com.org.careerbuilder.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.org.careerbuilder.dto.response.AdminFeeDtos;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Renders a single fee {@link AdminFeeDtos.ReceiptResponse} to a printable PDF,
 * mirroring the on-screen receipt card.
 */
final class AdminFeeReceiptPdfWriter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Color HEADER_BG = new Color(37, 99, 235);

    private AdminFeeReceiptPdfWriter() {
    }

    static byte[] write(AdminFeeDtos.ReceiptResponse r) {
        try {
            Document doc = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();

            String schoolName = r.schoolName() != null ? r.schoolName() : "School";
            Paragraph school = new Paragraph(schoolName,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, HEADER_BG));
            school.setAlignment(Element.ALIGN_CENTER);
            doc.add(school);

            Paragraph subtitle = new Paragraph("OFFICIAL FEE RECEIPT",
                    FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(14f);
            doc.add(subtitle);

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setSpacingAfter(12f);
            metaCell(meta, "Receipt No", r.receiptNumber());
            metaCell(meta, "Date", r.transactionDate() != null ? r.transactionDate().format(DATE_FMT) : "");
            metaCell(meta, "Student", r.studentName());
            metaCell(meta, "Class", r.classLabel());
            metaCell(meta, "Admission No", r.admissionNumber() != null ? r.admissionNumber() : "—");
            metaCell(meta, "Payment Mode", r.paymentMode());
            if (r.referenceNumber() != null && !r.referenceNumber().isBlank()) {
                metaCell(meta, "Reference", r.referenceNumber());
            }
            doc.add(meta);

            PdfPTable items = new PdfPTable(2);
            items.setWidthPercentage(100);
            items.setWidths(new int[]{70, 30});
            headerCell(items, "Description");
            headerCell(items, "Amount");
            if (r.items() != null) {
                for (AdminFeeDtos.ReceiptItem item : r.items()) {
                    bodyCell(items, item.description(), Element.ALIGN_LEFT);
                    bodyCell(items, "\u20B9" + item.amount().toPlainString(), Element.ALIGN_RIGHT);
                }
            }
            doc.add(items);

            Paragraph total = new Paragraph("Total Paid: " + r.totalPaidLabel(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, HEADER_BG));
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(10f);
            doc.add(total);

            Paragraph footer = new Paragraph(
                    "This is a system-generated receipt. No signature required.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(24f);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render receipt PDF", e);
        }
    }

    private static void metaCell(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        Phrase phrase = new Phrase();
        phrase.add(new Phrase(label + "\n", labelFont));
        phrase.add(new Phrase(value != null ? value : "", valueFont));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(0);
        cell.setPaddingBottom(8f);
        table.addCell(cell);
    }

    private static void headerCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private static void bodyCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }
}
