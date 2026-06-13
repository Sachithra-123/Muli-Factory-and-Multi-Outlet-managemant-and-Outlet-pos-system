package com.factory.management.controller;

import com.factory.management.entity.Factory;
import com.factory.management.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final EmployeeService employeeService;
    private final OutletService outletService;
    private final SaleService saleService;
    private final FactoryStockService factoryStockService;
    private final FactoryService factoryService;

    public DashboardController(ProductService productService, EmployeeService employeeService,
                               OutletService outletService, SaleService saleService,
                               FactoryStockService factoryStockService, FactoryService factoryService) {
        this.productService = productService;
        this.employeeService = employeeService;
        this.outletService = outletService;
        this.saleService = saleService;
        this.factoryStockService = factoryStockService;
        this.factoryService = factoryService;
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CASHIER"))) {
            return "redirect:/pos";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalEmployees", employeeService.countActive());
        model.addAttribute("totalOutlets", outletService.count());
        model.addAttribute("todayRevenue", saleService.getTodayRevenue());
        model.addAttribute("todaySales", saleService.getTodaySaleCount());
        model.addAttribute("lowStockItems", factoryStockService.getLowStockItems());
        
        List<com.factory.management.entity.Sale> recentSalesList = saleService.findAll().stream().limit(8).toList();
        model.addAttribute("recentSales", recentSalesList);

        // Data for Chart.js Revenue Trend (Line Chart)
        List<String> saleLabels = recentSalesList.stream()
                .map(s -> "Sale #" + s.getId()).collect(Collectors.toList());
        List<java.math.BigDecimal> saleData = recentSalesList.stream()
                .map(com.factory.management.entity.Sale::getTotalAmount).collect(Collectors.toList());
        model.addAttribute("saleLabels", saleLabels);
        model.addAttribute("saleData", saleData);

        // Data for Chart.js Factory Stock Doughnut Chart
        List<Factory> factories = factoryService.findAll();
        List<String> factoryNames = factories.stream()
                .map(Factory::getName).collect(Collectors.toList());
        List<Integer> factoryStockTotals = factories.stream()
                .map(f -> factoryService.getFactoryStock(f.getId()).stream()
                        .mapToInt(com.factory.management.entity.FactoryStock::getQuantity).sum())
                .collect(Collectors.toList());
        model.addAttribute("factoryNames", factoryNames);
        model.addAttribute("factoryStockTotals", factoryStockTotals);
        model.addAttribute("lowStockCount", factoryStockService.countLowStock());

        return "dashboard/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
