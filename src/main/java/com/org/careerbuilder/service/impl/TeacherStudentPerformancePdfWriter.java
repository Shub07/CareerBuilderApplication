package com.org.careerbuilder.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.org.careerbuilder.models.ExamResult;
import com.org.careerbuilder.models.Student;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

final class TeacherStudentPerformancePdfWriter {

    private TeacherStudentPerformancePdfWriter() {
    }

    static byte[] build(Student student, List<ExamResult> exams) throws DocumentException {
        Document doc = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();
        doc.add(new Paragraph("Student performance report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Student: " + student.getFirstName() + " " + student.getLastName()));
        doc.add(new Paragraph("Class: " + student.getClassName() + " " + student.getSection() + "   Roll: " + student.getRollNo()));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        for (String h : new String[]{"Exam", "Date", "Subject", "Marks", "Grade"}) {
            table.addCell(header(h));
        }
        for (ExamResult er : exams) {
            if (er.getObtainedMarks() == null) {
                continue;
            }
            table.addCell(cell(er.getExam().getName()));
            table.addCell(cell(er.getExam().getExamDate().toString()));
            table.addCell(cell(er.getSubject().getName()));
            table.addCell(cell(er.getObtainedMarks() + "/" + er.getTotalMarks()));
            table.addCell(cell(er.getGrade() != null ? er.getGrade() : ""));
        }
        doc.add(table);
        doc.close();
        return baos.toByteArray();
    }

    private static PdfPCell header(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        c.setBackgroundColor(new Color(230, 240, 255));
        c.setPadding(4);
        return c;
    }

    private static PdfPCell cell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", FontFactory.getFont(FontFactory.HELVETICA, 9)));
        c.setPadding(4);
        return c;
    }
}
