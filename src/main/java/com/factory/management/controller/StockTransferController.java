package com.factory.management.controller;

import com.factory.management.dto.StockTransferDTO;
import com.factory.management.service.FactoryService;
import com.factory.management.service.OutletService;
import com.factory.management.service.ProductService;
import com.factory.management.service.StockTransferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transfers")
public class StockTransferController {

    private final StockTransferService stockTransferService;
    private final ProductService productService;
    private final OutletService outletService;
    private final FactoryService factoryService;

    public StockTransferController(StockTransferService stockTransferService,
                                   ProductService productService,
                                   OutletService outletService,
                                   FactoryService factoryService) {
        this.stockTransferService = stockTransferService;
        this.productService = productService;
        this.outletService = outletService;
        this.factoryService = factoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("transfers", stockTransferService.findAll());
        return "transfer/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("transferDTO", new StockTransferDTO());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("outlets", outletService.findAll());
        model.addAttribute("factories", factoryService.findAll());
        return "transfer/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute StockTransferDTO dto, RedirectAttributes ra) {
        try {
            stockTransferService.transfer(dto);
            ra.addFlashAttribute("successMsg", "Stock transferred successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Transfer failed: " + e.getMessage());
        }
        return "redirect:/transfers";
    }
}
