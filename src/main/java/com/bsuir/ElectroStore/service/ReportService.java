package com.bsuir.ElectroStore.service;

import com.bsuir.ElectroStore.DTO.TopSellerDTO;
import com.bsuir.ElectroStore.model.Purchase;
import com.bsuir.ElectroStore.repository.PurchaseRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.TreeMap;


@Service
@RequiredArgsConstructor
public class ReportService {

    private final PurchaseRepository purchaseRepository;

    public void generateWordReport(List<TopSellerDTO> topSellers, LocalDate reportDate,
                                    HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=top_sellers.docx");

        XWPFDocument document = new XWPFDocument();

        // Заголовок
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText("Топ 3 продавца за неделю");
        titleRun.setBold(true);
        titleRun.setFontSize(16);

        // Даты периода
        LocalDate startOfWeek = reportDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = reportDate.with(DayOfWeek.SUNDAY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        XWPFParagraph dates = document.createParagraph();
        dates.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun datesRun = dates.createRun();
        datesRun.setText("Период: " + startOfWeek.format(formatter) + " - " + endOfWeek.format(formatter));
        datesRun.setFontSize(12);

        // Таблица с данными
        XWPFTable table = document.createTable();
        XWPFTableRow headerRow = table.getRow(0); // первая строка создаётся по умолчанию
        headerRow.getCell(0).setText("№");
        headerRow.addNewTableCell().setText("Продавец");
        headerRow.addNewTableCell().setText("Количество продаж");

// Стили для заголовков
        for (XWPFTableCell cell : headerRow.getTableCells()) {
            XWPFParagraph paragraph = cell.getParagraphs().get(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = paragraph.createRun();
            run.setBold(true);
            run.setFontSize(12);
        }

// Заполнение таблицы
        for (int i = 0; i < topSellers.size(); i++) {
            TopSellerDTO seller = topSellers.get(i);
            XWPFTableRow row = table.createRow();
            row.getCell(0).setText(String.valueOf(i + 1));
            row.getCell(1).setText(seller.getSurname() + " " + seller.getName());
            row.getCell(2).setText(String.valueOf(seller.getSalesCount()));
        }


        document.write(response.getOutputStream());
        document.close();
    }

    public ByteArrayInputStream generateWeeklyReport() throws IOException {
        // Получаем текущую дату
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        // Получаем все покупки
        List<Purchase> allPurchases = purchaseRepository.findAll();

        // Фильтруем по последним 7 дням и группируем по дате
        Map<LocalDate, Double> dailyTotals = allPurchases.stream()
                .filter(p -> {
                    try {
                        LocalDate purchaseDate = LocalDate.parse(p.getDate());
                        return !purchaseDate.isBefore(weekAgo) && !purchaseDate.isAfter(today);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.groupingBy(
                        p -> LocalDate.parse(p.getDate()),
                        TreeMap::new, // для сортировки по дате
                        Collectors.summingDouble(Purchase::getPayAmount)
                ));

        // Генерация Excel
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Отчёт за неделю");

            // Заголовок
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Дата");
            header.createCell(1).setCellValue("Сумма за день");

            int rowIdx = 1;
            double totalSum = 0;

            for (Map.Entry<LocalDate, Double> entry : dailyTotals.entrySet()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getKey().toString());
                row.createCell(1).setCellValue(entry.getValue());

                totalSum += entry.getValue();
            }

            // Итоговая строка
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("Итого за неделю:");
            totalRow.createCell(1).setCellValue(totalSum);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
