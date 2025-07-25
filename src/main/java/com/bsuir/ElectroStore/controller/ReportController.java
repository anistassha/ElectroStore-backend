package com.bsuir.ElectroStore.controller;

import com.bsuir.ElectroStore.DTO.TopSellerDTO;
import com.bsuir.ElectroStore.model.EmployeeData;
import com.bsuir.ElectroStore.model.Purchase;
import com.bsuir.ElectroStore.repository.PurchaseRepository;
import com.bsuir.ElectroStore.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final PurchaseRepository purchaseRepository;
    private final ReportService reportService;

    @GetMapping("/top-sellers")
    public void downloadTopSellersReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response) throws IOException {

        LocalDate reportDate = (date != null) ? date : LocalDate.now();
        List<TopSellerDTO> topSellers = calculateTopSellers(reportDate);
        reportService.generateWordReport(topSellers, reportDate, response);
    }

    private List<TopSellerDTO> calculateTopSellers(LocalDate reportDate) {
        LocalDate startOfWeek = reportDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = reportDate.with(DayOfWeek.SUNDAY);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = startOfWeek.format(formatter);
        String endDate = endOfWeek.format(formatter);

        List<Purchase> purchases = purchaseRepository.findByDateBetween(startDate, endDate);

        Map<EmployeeData, Long> salesByEmployee = purchases.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getUser().getEmployee(),
                        Collectors.counting()
                ));

        return salesByEmployee.entrySet().stream()
                .sorted(Map.Entry.<EmployeeData, Long>comparingByValue().reversed())
                .limit(3)
                .map(entry -> new TopSellerDTO(
                        entry.getKey().getName(),
                        entry.getKey().getSurname(),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/weekly")
    public ResponseEntity<InputStreamResource> downloadWeeklyReport() throws IOException {
        ByteArrayInputStream report = reportService.generateWeeklyReport();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=weekly_sales_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(report));
    }
}
