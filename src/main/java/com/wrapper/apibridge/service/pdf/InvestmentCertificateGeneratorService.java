package com.wrapper.apibridge.service.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.wrapper.apibridge.exception.PdfGenerationException;
import com.wrapper.apibridge.service.PersianUtilsService;
import com.wrapper.apibridge.service.dto.DocumentDTO;
import com.wrapper.apibridge.service.dto.InvestmentCertificateData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class InvestmentCertificateGeneratorService {
    private final String generatedFilesDir;
    private final PersianUtilsService persianUtilsService;

    private final static float CONTENT_TOP_MARGIN = 180f;
    private static final float CONTENT_BOTTOM_MARGIN = 50f;
    private static final float CONTENT_HORIZONTAL_MARGIN = 100f;
    private static final int CONTENT_LINE_SPACING = 18;

    public InvestmentCertificateGeneratorService(
            @Value("${app.pdf.investmentCertificateFilesDir}")
            String generatedFilesDir, PersianUtilsService persianUtilsService
    ) {
        this.persianUtilsService = persianUtilsService;
        if (!generatedFilesDir.endsWith("/")) {
            throw new IllegalArgumentException("generatedFilesDir must end with '/'");
        }
        this.generatedFilesDir = generatedFilesDir;
    }

    public DocumentDTO generate(String fileName, InvestmentCertificateData data) {
        validateData(data);

        String documentPath = generatedFilesDir + fileName;
        Document document = null;
        PdfWriter pdfWriter = null;

        try {
            document = new Document(PageSize.A4, 36, 36, 36, 36);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(documentPath));
            document.open();

            // Persian fonts initialization
            BaseFont vazirFont = BaseFont.createFont("fonts/Vazir.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font headerFont = new Font(vazirFont, 12, Font.BOLD, new BaseColor(40, 40, 40));
            Font normalFont = new Font(vazirFont, 11, Font.NORMAL, BaseColor.BLACK);
            Font smallFont = new Font(vazirFont, 9, Font.NORMAL, BaseColor.GRAY);
            Font tableHeaderFont = new Font(vazirFont, 10, Font.BOLD, BaseColor.BLACK);
            Font tableCellFont = new Font(vazirFont, 9, Font.NORMAL, BaseColor.BLACK);
            Font companyNameFont = new Font(vazirFont, 12, Font.NORMAL, new BaseColor(0.161f, 0.384f, 0.627f));

            // First page - add all decorations
            addPageDecorations(pdfWriter, smallFont, companyNameFont, data);

            // Create ColumnText and add content ONCE
            ColumnText ct = createColumnTextForMainContent(pdfWriter);
            addHeaderAndFirstParagraph(ct, headerFont, normalFont, data);
            addInvestmentTable(ct, tableHeaderFont, tableCellFont, data);
            addBulletPoints(ct, normalFont, data);
            addSignatureTable(ct, smallFont);

            // Render content and capture status
            int status = ct.go(false);

            // Handle overflow - create new pages as needed
            while (ColumnText.hasMoreText(status)) {
                document.newPage();
                addPageDecorations(pdfWriter, smallFont, companyNameFont, data);

                // CRITICAL: Update the canvas and reset column bounds using constants
                ct.setCanvas(pdfWriter.getDirectContent());
                float startY = PageSize.A4.getHeight() - CONTENT_TOP_MARGIN;
                float endY = CONTENT_BOTTOM_MARGIN;
                ct.setSimpleColumn(
                        CONTENT_HORIZONTAL_MARGIN,
                        endY,
                        PageSize.A4.getWidth() - CONTENT_HORIZONTAL_MARGIN,
                        startY,
                        CONTENT_LINE_SPACING,
                        Element.ALIGN_JUSTIFIED
                );

                status = ct.go(false); // Continue rendering
            }

            return new DocumentDTO(documentPath);
        } catch (DocumentException | IOException e) {
            deleteDocument(documentPath);
            throw new PdfGenerationException(e);
        } finally {
            if (document != null && document.isOpen()) {
                try {
                    document.close();
                } catch (Exception ignored) {
                }
            }
            if (pdfWriter != null) {
                try {
                    pdfWriter.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void validateData(InvestmentCertificateData data) {
        // Investor Name - most critical for layout
        if (data.getInvestorName().length() > 60) {
            throw new IllegalArgumentException("investorName exceeds maximum length of 60 characters");
        }

        // Bank Account Owner Name
        if (data.getBankAccountOwnerName().length() > 60) {
            throw new IllegalArgumentException("bankAccountOwnerName exceeds maximum length of 60 characters");
        }

        // Document Number
        if (data.getDocumentNumber().length() > 20) {
            throw new IllegalArgumentException("documentNumber exceeds maximum length of 20 characters");
        }

        // Contract Number
        if (data.getContractNumber().length() > 50) {
            throw new IllegalArgumentException("contractNumber exceeds maximum length of 50 characters");
        }

        // Bank Name
        if (data.getBankName().length() > 60) {
            throw new IllegalArgumentException("bankName exceeds maximum length of 60 characters");
        }

        // Bank Account Number
        if (data.getBankAccountNumber().length() > 40) {
            throw new IllegalArgumentException("bankAccountNumber exceeds maximum length of 40 characters");
        }

        // Company National ID
        if (data.getCompanyNationalId().length() > 30) {
            throw new IllegalArgumentException("companyNationalId exceeds maximum length of 30 characters");
        }

        // Investment Amount
        if (data.getInvestmentAmountRials() < 0) {
            throw new IllegalArgumentException("investmentAmountRials cannot be negative");
        }

        if (data.getInvestmentAmountRials() > 999_999_999_999_999L) {
            throw new IllegalArgumentException("investmentAmountRials exceeds maximum value");
        }
    }

    private void deleteDocument(String documentPath) {
        File documentFile = new File(documentPath);
        if (!documentFile.exists()) {
            return;
        }
        documentFile.delete();
    }

    private ColumnText createColumnTextForMainContent(PdfWriter pdfWriter) {
        ColumnText ct = new ColumnText(pdfWriter.getDirectContent());
        ct.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        float startY = PageSize.A4.getHeight() - CONTENT_TOP_MARGIN; // Adjust based on logo position
        float endY = CONTENT_BOTTOM_MARGIN; // Height for content=
        ct.setSimpleColumn(
                CONTENT_HORIZONTAL_MARGIN,
                endY,
                PageSize.A4.getWidth() - CONTENT_HORIZONTAL_MARGIN,
                startY,
                CONTENT_LINE_SPACING, // Leading (line spacing)
                Element.ALIGN_JUSTIFIED
        );
        return ct;
    }

    private void addVerticalBox(PdfContentByte canvas, Font font) throws DocumentException {
        canvas.saveState();

        float boxWidth = 25f;
        float boxHeight = 220f;
        float x = 55f;
        float y = 180f;

        // Draw border box
        canvas.setLineWidth(0.8f);
        canvas.rectangle(x, y, boxWidth, boxHeight);
        canvas.stroke();
        canvas.setColorStroke(BaseColor.GRAY);

        // Create a rotated ColumnText for proper RTL rendering
        PdfTemplate tpl = canvas.createTemplate(boxHeight, boxWidth); // swapped width/height due to rotation
        ColumnText verticalCt = new ColumnText(tpl);
        verticalCt.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Paragraph verticalText = new Paragraph("این گواهی بدون هلوگرام و مهر و امضا فاقد اعتبار است.",
                font);
        verticalText.setAlignment(Element.ALIGN_CENTER);
        verticalCt.setSimpleColumn(
                0, 0, boxHeight, boxWidth, 12, Element.ALIGN_CENTER);
        verticalCt.addElement(verticalText);
        verticalCt.go();

        // Rotate 90 degrees to place vertically upright
        canvas.addTemplate(tpl, 0, 1, -1, 0, x + boxWidth, y);
        canvas.restoreState();
    }

    private void addSignatureTable(ColumnText ct, Font font) throws DocumentException {
        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        sigTable.setWidthPercentage(100);
        sigTable.setWidths(new float[]{1f, 1f});
        sigTable.setSpacingBefore(40f);
        sigTable.setSpacingAfter(20f);

        // Right cell - امضاء اول
        Paragraph sig1 = new Paragraph("امضاء اول\n(مدیرعامل)", font);
        sig1.setAlignment(Element.ALIGN_CENTER);
        PdfPCell cell1 = new PdfPCell(sig1);
        cell1.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell1.setPadding(10f);
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setMinimumHeight(60f);
        sigTable.addCell(cell1);

        // Left cell - امضاء دوم
        Paragraph sig2 = new Paragraph("امضاء دوم\n(عضو هیئت‌مدیره)", font);
        sig2.setAlignment(Element.ALIGN_CENTER);
        PdfPCell cell2 = new PdfPCell(sig2);
        cell2.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell2.setPadding(10f);
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setMinimumHeight(60f);
        sigTable.addCell(cell2);

        ct.addElement(sigTable);
    }

    private void addBulletPoints(ColumnText ct, Font normalFont, InvestmentCertificateData data) {
        // Create RTL bullet list
        List bulletList = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
        bulletList.setListSymbol("• ");
        bulletList.setSymbolIndent(20f);
        bulletList.setIndentationRight(20f); // RTL indentation
        bulletList.setAlignindent(false);

        ListItem item1 = createListItem(
                String.format(
                        "تمام مبالغ جدول فوق به حساب شماره %s نزد بانک %s " +
                                "متعلق به جناب آقای %s واریز می‌گردد.",
                        persianUtilsService.convertTextNumbersToPersian(data.getBankAccountNumber()),
                        data.getBankName(),
                        data.getBankAccountOwnerName()
                ),
                normalFont
        );

        ListItem item2 = createListItem(
                "مبالغ مذکور در جدول فوق حداقل ظرف ۵ روز کاری پس از سررسید چک پرداخت می‌شود.",
                normalFont
        );

        ListItem item3 = createListItem(
                "پرداخت مبالغ این گواهی، توسط صندوق توسعه فناوری‌های راهبردی تضمین می‌گردد.",
                normalFont
        );

        bulletList.add(item1);
        bulletList.add(item2);
        bulletList.add(item3);

        ct.addElement(bulletList);
    }

    private ListItem createListItem(String text, Font font) {
        ListItem item = new ListItem(
                text,
                font
        );
        item.setAlignment(Element.ALIGN_LEFT);

        return item;
    }

    private void addInvestmentTable(ColumnText ct, Font headerFont, Font cellFont, InvestmentCertificateData data) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        table.setTotalWidth(new float[]{70f, 90f, 80f, 130f, 60f});
        table.setLockedWidth(true);
        table.setSpacingBefore(15f);
        table.setSpacingAfter(20f);
        table.setHeaderRows(1);

        // Header row
        addHeaderCell(table, "ردیف", headerFont);
        addHeaderCell(table, "مبلغ چک (ریال)", headerFont);
        addHeaderCell(table, "تاریخ چک", headerFont);
        addHeaderCell(table, "شماره چک", headerFont);
        addHeaderCell(table, "شماره سند", headerFont);


        long totalChequeAmount = 0;
        // Data rows
        for (int i = 0; i < data.getCheques().size(); i++) {
            var cheque = data.getCheques().get(i);
            addDataRow(
                    table,
                    persianUtilsService.convertTextNumbersToPersian(String.valueOf(i + 1)),
                    persianUtilsService.formatPersianRials(cheque.getAmount()),
                    persianUtilsService.convertTextNumbersToPersian(cheque.getDate()),
                    persianUtilsService.convertTextNumbersToPersian(cheque.getNumber()),
                    persianUtilsService.convertTextNumbersToPersian(cheque.getDocumentNumber()),
                    cellFont
            );

            totalChequeAmount += cheque.getAmount();
        }


        // Total row

        PdfPCell totalLabel = new PdfPCell(new Phrase("جمع کل", headerFont));
        totalLabel.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        totalLabel.setColspan(1);
        totalLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totalLabel.setPadding(6f);
        totalLabel.setBackgroundColor(new BaseColor(240, 240, 240));
        totalLabel.setBorderWidth(0.5f);
        table.addCell(totalLabel);

        PdfPCell totalAmount = new PdfPCell(new Phrase(persianUtilsService.formatPersianRials(totalChequeAmount) + " ریال", headerFont));
        totalAmount.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        totalAmount.setColspan(1);
        totalAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalAmount.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totalAmount.setPadding(6f);
        totalAmount.setBackgroundColor(new BaseColor(240, 240, 240));
        totalAmount.setBorderWidth(0.5f);
        table.addCell(totalAmount);

        PdfPCell blankCell = new PdfPCell();
        blankCell.setBorderWidth(0.0f);

        table.addCell(blankCell);
        table.addCell(blankCell);
        table.addCell(blankCell);

        ct.addElement(table);
    }


    // Helper: add header cell
    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    // Helper: add data row
    private void addDataRow(PdfPTable table, String col1, String col2,
                            String col3, String col4, String col5, Font font) {
        String[] values = {col1, col2, col3, col4, col5};
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, font));
            cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5f);
            cell.setBorderColor(new BaseColor(150, 150, 150));
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }
    }

    private void addHeaderAndFirstParagraph(ColumnText ct, Font headerFont, Font normalFont, InvestmentCertificateData data) throws DocumentException {
        Paragraph header = new Paragraph("گواهی سرمایه‌گذاری", headerFont);
        header.setAlignment(Paragraph.ALIGN_CENTER);
        header.setSpacingAfter(20f);

        Paragraph paragraph = new Paragraph(
                String.format(
                        "بدین‌وسیله گواهی می‌گردد جناب آقای/خانم %s " +
                                "در پروژه خرید دین صندوق توسعه فناوری‌های راهبردی به شماره " +
                                "قرارداد %s به ظرفیت شرکت تجهیز گستر شریف به شناسه ملی " +
                                "%s به شرح جدول ذیل %s ریال سرمایه‌گذاری نموده‌اند.",
                        data.getInvestorName(),
                        persianUtilsService.convertTextNumbersToPersian(data.getContractNumber()),
                        persianUtilsService.convertTextNumbersToPersian(data.getCompanyNationalId()),
                        persianUtilsService.formatPersianRials(data.getInvestmentAmountRials())
                ),
                normalFont);

        paragraph.setAlignment(Paragraph.ALIGN_JUSTIFIED);
        paragraph.setSpacingAfter(20f);

        ct.addElement(header);
        ct.addElement(paragraph);
    }

    private void addWatermarkLogo(PdfContentByte canvas) throws DocumentException, IOException {
        try {
            Image watermark = Image.getInstance("src/main/resources/images/logo.png");

            float watermarkWidth = 300f;
            float watermarkHeight = 240f;
            watermark.scaleAbsolute(watermarkWidth, watermarkHeight);

            float centerX = (PageSize.A4.getWidth() - watermarkWidth) / 2;
            float centerY = (PageSize.A4.getHeight() - watermarkHeight) / 2;

            watermark.setAbsolutePosition(centerX, centerY);

            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.1f);  // 10% opacity
            gs.setStrokeOpacity(0.1f);

            canvas.saveState();
            canvas.setGState(gs);
            canvas.addImage(watermark);
            canvas.restoreState();

        } catch (IOException | DocumentException e) {
            System.err.println("Error adding watermark: " + e.getMessage());
            throw e;
        }
    }

    private void addLogoAndDate(PdfContentByte canvas, Font dateFont, Font companyNameFont, InvestmentCertificateData data) throws DocumentException, IOException {
        try {
            Image logo = Image.getInstance("src/main/resources/images/logo.png");

            float logoWidth = 112.5f;
            float logoHeight = 90f;
            logo.scaleAbsolute(logoWidth, logoHeight);

            float centerX = (PageSize.A4.getWidth() - logoWidth) / 2;
            float topMargin = 60f;
            float centerY = PageSize.A4.getHeight() - topMargin - logoHeight;

            logo.setAbsolutePosition(centerX, centerY);
            canvas.addImage(logo);

            ColumnText companyCT = new ColumnText(canvas);
            companyCT.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            Paragraph companyName = new Paragraph("صندوق پژوهش و فناوری\nتوسعه فناوری‌های راهبردی", companyNameFont);
            companyName.setAlignment(Element.ALIGN_CENTER);
            companyName.setLeading(14f);

            float companyNameY = centerY + 10f; // Position below logo
            companyCT.setSimpleColumn(
                    centerX - 50f,
                    companyNameY - 30f,
                    centerX + logoWidth + 50f,
                    companyNameY,
                    14,
                    Element.ALIGN_CENTER
            );

            companyCT.addElement(companyName);
            companyCT.go();

            // Date text (existing code)
            ColumnText dateCT = new ColumnText(canvas);
            dateCT.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            Paragraph dateText = new Paragraph(String.format(
                    "تاریخ: %s\nشماره: %s",
                    persianUtilsService.getCurrentPersianDateString("dd/MM/yyyy"),
                    persianUtilsService.convertTextNumbersToPersian(data.getDocumentNumber())
            ), dateFont);
            dateText.setAlignment(Element.ALIGN_RIGHT);

            float leftMargin = 70f;
            dateCT.setSimpleColumn(
                    leftMargin,
                    centerY,
                    leftMargin + 150f,
                    centerY + logoHeight,
                    12,
                    Element.ALIGN_RIGHT
            );

            dateCT.addElement(dateText);
            dateCT.go();

        } catch (IOException | DocumentException e) {
            System.err.println("Error adding logo: " + e.getMessage());
            throw e;
        }
    }


    private void addInTheNameOfGod(PdfContentByte canvas, Font font) throws DocumentException {
        ColumnText headerCT = new ColumnText(canvas);
        headerCT.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Paragraph footer = new Paragraph(
                "بسم تعالی",
                font);
        footer.setAlignment(Element.ALIGN_CENTER);

        float topMargin = 30f;
        headerCT.setSimpleColumn(
                50f,              // left
                PageSize.A4.getHeight() - topMargin - 50f,     // bottom position
                PageSize.A4.getWidth() - 50f,  // right
                PageSize.A4.getHeight() - topMargin,             // top of footer area
                0, Element.ALIGN_CENTER);

        headerCT.addElement(footer);
        headerCT.go();
    }

    private void addDecorativeCorners(PdfContentByte canvas) throws IOException, DocumentException {
        float margin = 20;
        float size = 65;

        Image cornerImg = Image.getInstance("src/main/resources/images/corners.png");
        cornerImg.scaleAbsolute(size, size);

        cornerImg.setAbsolutePosition(margin, PageSize.A4.getHeight() - margin - size);
        canvas.addImage(cornerImg);

        cornerImg.setRotationDegrees(270);
        cornerImg.setAbsolutePosition(PageSize.A4.getWidth() - margin - size, PageSize.A4.getHeight() - margin - size);
        canvas.addImage(cornerImg);

        cornerImg.setRotationDegrees(180);
        cornerImg.setAbsolutePosition(PageSize.A4.getWidth() - margin - size, margin);
        canvas.addImage(cornerImg);

        cornerImg.setRotationDegrees(90);
        cornerImg.setAbsolutePosition(margin, margin);
        canvas.addImage(cornerImg);
    }

    private void addAddress(PdfContentByte canvas, Font font) throws IOException, DocumentException {
        ColumnText footerCT = new ColumnText(canvas);
        footerCT.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

        Paragraph footer = new Paragraph(
                "نشانی: تهران، میدان آرژانتین، خیابان خدامی، نبش خیابان عدالتی، پلاک ۴، طبقه ۲ و ۳\n" +
                        "کد پستی: ۱۲۳۴۵۶۷۸۹۱۰",
                font);
        footer.setAlignment(Element.ALIGN_CENTER);

        float bottomMargin = 10f;
        footerCT.setSimpleColumn(
                50f,              // left
                bottomMargin,     // bottom position
                PageSize.A4.getWidth() - 50f,  // right
                bottomMargin + 50f,             // top of footer area
                0, Element.ALIGN_CENTER);

        footerCT.addElement(footer);
        footerCT.go();
    }

    private void addPageDecorations(PdfWriter pdfWriter, Font smallFont,
                                    Font companyNameFont, InvestmentCertificateData data)
            throws DocumentException, IOException {
        PdfContentByte canvas = pdfWriter.getDirectContent();
        addDecorativeCorners(canvas);
        addWatermarkLogo(canvas);
        addInTheNameOfGod(canvas, smallFont);
        addLogoAndDate(canvas, smallFont, companyNameFont, data);
        addAddress(canvas, smallFont);
        addVerticalBox(canvas, smallFont);
    }

}
