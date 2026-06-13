package com.factory.management.controller;

import com.factory.management.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final SaleService saleService;
    private final FactoryStockService factoryStockService;
    private final StockTransferService stockTransferService;
    private final OutletService outletService;
    private final com.factory.management.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public ReportController(SaleService saleService, FactoryStockService factoryStockService,
                            StockTransferService stockTransferService, OutletService outletService,
                            com.factory.management.repository.UserRepository userRepository,
                            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.saleService = saleService;
        this.factoryStockService = factoryStockService;
        this.stockTransferService = stockTransferService;
        this.outletService = outletService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/sales")
    public String salesReport(@RequestParam(required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              Model model) {
        LocalDate reportDate = (date != null) ? date : LocalDate.now();
        model.addAttribute("reportDate", reportDate);
        model.addAttribute("sales", saleService.findByDate(reportDate));
        model.addAttribute("totalRevenue", saleService.findByDate(reportDate)
                .stream().map(s -> s.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return "report/sales";
    }

    @GetMapping("/stock")
    public String stockReport(Model model) {
        model.addAttribute("factoryStock", factoryStockService.findAll());
        model.addAttribute("outlets", outletService.findAll());
        return "report/stock";
    }

    @GetMapping("/transfers")
    public String transferReport(@RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                 Model model) {
        if (from != null && to != null) {
            model.addAttribute("transfers", stockTransferService.findByDateRange(
                    from.atStartOfDay(), to.atTime(23, 59, 59)));
            model.addAttribute("from", from);
            model.addAttribute("to", to);
        } else {
            model.addAttribute("transfers", stockTransferService.findAll());
        }
        return "report/transfers";
    }

    @GetMapping("/z-report")
    public String zReport(Model model, org.springframework.security.core.Authentication auth) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<com.factory.management.entity.Sale> todaySales = saleService.findByDate(today);
        
        model.addAttribute("reportDate", today);
        model.addAttribute("salesCount", todaySales.size());
        model.addAttribute("totalRevenue", todaySales.stream()
                .map(s -> s.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        
        // Group by payment method
        java.util.Map<String, java.math.BigDecimal> paymentBreakdown = todaySales.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        com.factory.management.entity.Sale::getPaymentMethod,
                        java.util.stream.Collectors.mapping(
                                com.factory.management.entity.Sale::getTotalAmount,
                                java.util.stream.Collectors.reducing(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                        )
                ));
        model.addAttribute("payments", paymentBreakdown);
        model.addAttribute("cashierName", auth.getName());
        
        return "report/z_report";
    }

    @PostMapping("/verify-z")
    public String verifyAndRedirect(@RequestParam String password, 
                                    org.springframework.security.core.Authentication auth,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        com.factory.management.entity.User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return "redirect:/reports/z-report";
        } else {
            ra.addFlashAttribute("error", "Invalid password! Identity verification failed.");
            return "redirect:/pos";
        }
    }
}
