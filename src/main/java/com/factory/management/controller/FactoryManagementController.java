package com.factory.management.controller;

import com.factory.management.entity.Factory;
import com.factory.management.service.FactoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/factories")
public class FactoryManagementController {

    private final FactoryService factoryService;

    public FactoryManagementController(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("factories", factoryService.findAll());
        return "factory/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("factory", new Factory());
        model.addAttribute("pageTitle", "Add Factory");
        return "factory/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Factory factory, RedirectAttributes ra) {
        try {
            factoryService.save(factory);
            ra.addFlashAttribute("successMsg", "Factory added successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/factories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("factory", factoryService.findById(id));
        model.addAttribute("pageTitle", "Edit Factory");
        return "factory/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Factory factory, RedirectAttributes ra) {
        try {
            factoryService.update(id, factory);
            ra.addFlashAttribute("successMsg", "Factory updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/factories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            factoryService.delete(id);
            ra.addFlashAttribute("successMsg", "Factory deleted!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Cannot delete factory – it may have linked stock or employees.");
        }
        return "redirect:/factories";
    }

    @GetMapping("/{id}/stock")
    public String viewStock(@PathVariable Long id, Model model) {
        model.addAttribute("factory", factoryService.findById(id));
        model.addAttribute("stockList", factoryService.getFactoryStock(id));
        return "factory/stock";
    }
}
