package com.bsuir.ElectroStore.controller;

import com.bsuir.ElectroStore.DTO.DailyRevenue;
import com.bsuir.ElectroStore.DTO.TopSellerDTO;
import com.bsuir.ElectroStore.model.EmployeeData;
import com.bsuir.ElectroStore.model.Purchase;
import com.bsuir.ElectroStore.model.Product;
import com.bsuir.ElectroStore.repository.PurchaseRepository;
import com.bsuir.ElectroStore.repository.ProductRepository;
import com.lowagie.text.Cell;
import com.lowagie.text.Font;
import com.lowagie.text.Row;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lowagie.text.Document;

import com.lowagie.text.*;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public PurchaseController(PurchaseRepository purchaseRepository, ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    // Получить все покупки
    @GetMapping
    public ResponseEntity<List<Purchase>> getAllPurchases() {
        return ResponseEntity.ok(purchaseRepository.findAll());
    }

    // Получить покупку по ID
    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getPurchaseById(@PathVariable int id) {
        return purchaseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Добавить новую покупку с уменьшением количества товара
    @PostMapping
    public ResponseEntity<?> createPurchase(@RequestBody Purchase purchase) {
        Product product = productRepository.findById(purchase.getProduct().getProductId())
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // Проверка наличия товара на складе
        if (product.getStockQuantity() < purchase.getQuantity()) {
            return ResponseEntity.badRequest().body("Недостаточно товара на складе");
        }

        // Уменьшаем количество на складе
        product.setStockQuantity(product.getStockQuantity() - purchase.getQuantity());
        productRepository.save(product);

        // Сохраняем покупку
        Purchase savedPurchase = purchaseRepository.save(purchase);
        return ResponseEntity.ok(savedPurchase);
    }

    // Обновить покупку
    @PutMapping("/{id}")
    public ResponseEntity<Purchase> updatePurchase(@PathVariable int id, @RequestBody Purchase updatedPurchase) {
        return purchaseRepository.findById(id)
                .map(purchase -> {
                    purchase.setProduct(updatedPurchase.getProduct());
                    purchase.setCustomer(updatedPurchase.getCustomer());
                    purchase.setUser(updatedPurchase.getUser());
                    purchase.setDate(updatedPurchase.getDate());
                    purchase.setQuantity(updatedPurchase.getQuantity());
                    purchase.setPayAmount(updatedPurchase.getPayAmount());
                    return ResponseEntity.ok(purchaseRepository.save(purchase));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Удалить покупку
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@PathVariable int id) {
        if (purchaseRepository.existsById(id)) {
            purchaseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Purchase>> getPurchasesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(purchaseRepository.findByUserId(userId));
    }

    @GetMapping("/{id}/receipt")
    public void getPurchaseReceipt(@PathVariable int id, HttpServletResponse response)
            throws IOException, DocumentException {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Покупка не найдена"));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=receipt_" + id + ".pdf");

        Document document = new Document(PageSize.A4, 36, 36, 60, 36); // Отступы в пунктах
        PdfWriter.getInstance(document, response.getOutputStream());

        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 18, Font.BOLD, new Color(0, 51, 102)); // Темно-синий
        Font headerFont = new Font(bf, 12, Font.BOLD, Color.BLACK);
        Font contentFont = new Font(bf, 10, Font.NORMAL, Color.DARK_GRAY);
        Font totalFont = new Font(bf, 12, Font.BOLD, new Color(0, 102, 0)); // Темно-зеленый

        document.open();

        Paragraph title = new Paragraph("ЧЕК О ПРОДАЖЕ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Paragraph shopInfo = new Paragraph("Магазин: \"ELECTROSTORE\"\nАдрес: пр. Независимости, 1", contentFont);
        shopInfo.setAlignment(Element.ALIGN_CENTER);
        shopInfo.setSpacingAfter(15);
        document.add(shopInfo);

        LineSeparator line = new LineSeparator();
        line.setLineWidth(1f);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        table.setWidths(new float[]{30, 70});

        addTableRow(table, "Номер чека:", "#" + id, headerFont, contentFont);
        addTableRow(table, "Дата продажи:", purchase.getDate().toString(), headerFont, contentFont);
        addTableRow(table, "Кассир:", purchase.getUser().getEmployee().getName() + " " +
                purchase.getUser().getEmployee().getSurname(), headerFont, contentFont);
        addTableRow(table, "Клиент:", purchase.getCustomer().getFirstName() + " " +
                purchase.getCustomer().getLastName(), headerFont, contentFont);
        addTableRow(table, "Товар:", purchase.getProduct().getProductName(), headerFont, contentFont);
        addTableRow(table, "Количество:", purchase.getQuantity() + "", headerFont, contentFont);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("ИТОГО:", headerFont));
        totalLabelCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(purchase.getPayAmount() + " BYN", totalFont));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setBorder(Rectangle.NO_BORDER);

        table.addCell(totalLabelCell);
        table.addCell(totalValueCell);

        document.add(table);

        Paragraph footer = new Paragraph(
                "Спасибо за покупку!\n" +
                        "Телефон для справок: +375 (33) 648-10-29\n" +
                        "Возврат товара в течение 14 дней при наличии чека",
                new Font(bf, 9, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);

        document.close();
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

//    @GetMapping("/top-sellers")
//    public void getWeeklyTopSellersWordReport(
//            @RequestParam(required = false)
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//            HttpServletResponse response) throws IOException {
//
//        LocalDate reportDate = (date != null) ? date : LocalDate.now();
//        List<TopSellerDTO> topSellers = calculateTopSellers(reportDate);
//        generateWordReport(topSellers, reportDate, response);
//    }
//
//    private List<TopSellerDTO> calculateTopSellers(LocalDate reportDate) {
//        // Определяем границы недели
//        LocalDate startOfWeek = reportDate.with(DayOfWeek.MONDAY);
//        LocalDate endOfWeek = reportDate.with(DayOfWeek.SUNDAY);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String startDate = startOfWeek.format(formatter);
//        String endDate = endOfWeek.format(formatter);
//
//        // Получаем все покупки за период
//        List<Purchase> purchases = purchaseRepository.findByDateBetween(startDate, endDate);
//
//        // Группируем по продавцам и считаем количество продаж
//        Map<EmployeeData, Long> salesByEmployee = purchases.stream()
//                .collect(Collectors.groupingBy(
//                        p -> p.getUser().getEmployee(),
//                        Collectors.counting()
//                ));
//
//        // Сортируем по количеству продаж (по убыванию) и берем топ-3
//        return salesByEmployee.entrySet().stream()
//                .sorted(Map.Entry.<EmployeeData, Long>comparingByValue().reversed())
//                .limit(3)
//                .map(entry -> new TopSellerDTO(
//                        entry.getKey().getName(),
//                        entry.getKey().getSurname(),
//                        entry.getValue()
//                ))
//                .collect(Collectors.toList());
//    }
//
//    private void generateWordReport(List<TopSellerDTO> topSellers, LocalDate reportDate,
//                                    HttpServletResponse response) throws IOException {
//        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//        response.setHeader("Content-Disposition", "attachment; filename=top_sellers.docx");
//
//        XWPFDocument document = new XWPFDocument();
//
//        // Заголовок
//        XWPFParagraph title = document.createParagraph();
//        title.setAlignment(ParagraphAlignment.CENTER);
//        XWPFRun titleRun = title.createRun();
//        titleRun.setText("Топ 3 продавцов за неделю");
//        titleRun.setBold(true);
//        titleRun.setFontSize(16);
//
//        // Даты периода
//        LocalDate startOfWeek = reportDate.with(DayOfWeek.MONDAY);
//        LocalDate endOfWeek = reportDate.with(DayOfWeek.SUNDAY);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//
//        XWPFParagraph dates = document.createParagraph();
//        dates.setAlignment(ParagraphAlignment.CENTER);
//        XWPFRun datesRun = dates.createRun();
//        datesRun.setText("Период: " + startOfWeek.format(formatter) + " - " + endOfWeek.format(formatter));
//        datesRun.setFontSize(12);
//
//        // Таблица с данными
//        XWPFTable table = document.createTable();
//        XWPFTableRow headerRow = table.getRow(0); // первая строка создаётся по умолчанию
//        headerRow.getCell(0).setText("№");
//        headerRow.addNewTableCell().setText("Продавец");
//        headerRow.addNewTableCell().setText("Количество продаж");
//
//// Стили для заголовков
//        for (XWPFTableCell cell : headerRow.getTableCells()) {
//            XWPFParagraph paragraph = cell.getParagraphs().get(0);
//            paragraph.setAlignment(ParagraphAlignment.CENTER);
//            XWPFRun run = paragraph.createRun();
//            run.setBold(true);
//            run.setFontSize(12);
//        }
//
//// Заполнение таблицы
//        for (int i = 0; i < topSellers.size(); i++) {
//            TopSellerDTO seller = topSellers.get(i);
//            XWPFTableRow row = table.createRow();
//            row.getCell(0).setText(String.valueOf(i + 1));
//            row.getCell(1).setText(seller.getSurname() + " " + seller.getName());
//            row.getCell(2).setText(String.valueOf(seller.getSalesCount()));
//        }
//
//
//        document.write(response.getOutputStream());
//        document.close();
//    }


}



