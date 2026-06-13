package com.factory.management.controller;

import com.factory.management.service.FactoryStockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/factory-stock")
public class FactoryStockController {

    private final FactoryStockService factoryStockService;

    public FactoryStockController(FactoryStockService factoryStockService) {
        this.factoryStockService = factoryStockService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("stockList", factoryStockService.findAll());
        model.addAttribute("lowStockCount", factoryStockService.countLowStock());
        return "stock/factory";
    }

    @PostMapping("/adjust/{id}")
    public String adjustStock(@PathVariable Long id,
                              @RequestParam int quantity,
                              RedirectAttributes ra) {
        try {
            factoryStockService.adjustStock(id, quantity);
            ra.addFlashAttribute("successMsg", "Stock updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/factory-stock";
    }

    @PostMapping("/add/{id}")
    public String addStock(@PathVariable Long id,
                           @RequestParam int addQty,
                           RedirectAttributes ra) {
        try {
            factoryStockService.addStock(id, addQty);
            ra.addFlashAttribute("successMsg", "Stock increased successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/factory-stock";
    }

    @PostMapping("/threshold/{id}")
    public String updateThreshold(@PathVariable Long id,
                                  @RequestParam int threshold,
                                  RedirectAttributes ra) {
        try {
            factoryStockService.updateThreshold(id, threshold);
            ra.addFlashAttribute("successMsg", "Low stock threshold updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/factory-stock";
    }
}
