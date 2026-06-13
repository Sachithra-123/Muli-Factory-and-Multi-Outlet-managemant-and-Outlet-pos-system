package com.factory.management.controller;

import com.factory.management.dto.SaleRequestDTO;
import com.factory.management.entity.Sale;
import com.factory.management.service.OutletService;
import com.factory.management.service.SaleService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SaleController {

    private final SaleService saleService;
    private final OutletService outletService;
    private final com.factory.management.repository.UserRepository userRepository;

    public SaleController(SaleService saleService, OutletService outletService, 
                          com.factory.management.repository.UserRepository userRepository) {
        this.saleService = saleService;
        this.outletService = outletService;
        this.userRepository = userRepository;
    }

    @GetMapping("/pos")
    public String posPage(Model model, Authentication auth) {
        model.addAttribute("outlets", outletService.findAll());
        
        String fullName = auth.getName();
        userRepository.findByUsername(auth.getName()).ifPresent(u -> {
            if (u.getFullName() != null && !u.getFullName().isEmpty()) {
                model.addAttribute("cashierName", u.getFullName());
            } else {
                model.addAttribute("cashierName", u.getUsername());
            }
        });
        
        return "pos/sale";
    }

    @PostMapping("/pos/complete")
    @ResponseBody
    public java.util.Map<String, Object> completeSale(@RequestBody SaleRequestDTO request,
                                                       Authentication auth) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Sale sale = saleService.completeSale(request, auth.getName());
            response.put("success", true);
            response.put("saleId", sale.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/pos/bill/{id}")
    public String bill(@PathVariable Long id, Model model) {
        Sale sale = saleService.findById(id);
        model.addAttribute("sale", sale);
        return "pos/bill";
    }

    @GetMapping("/sales")
    public String salesHistory(Model model) {
        model.addAttribute("sales", saleService.findAll());
        return "pos/history";
    }
}
