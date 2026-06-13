package com.factory.management.controller;

import com.factory.management.entity.Outlet;
import com.factory.management.service.OutletService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/outlets")
public class OutletController {

    private final OutletService outletService;

    public OutletController(OutletService outletService) {
        this.outletService = outletService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("outlets", outletService.findAll());
        return "outlet/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("outlet", new Outlet());
        model.addAttribute("pageTitle", "Add Outlet");
        return "outlet/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Outlet outlet, RedirectAttributes ra) {
        try {
            outletService.save(outlet);
            ra.addFlashAttribute("successMsg", "Outlet added successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/outlets";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("outlet", outletService.findById(id));
        model.addAttribute("pageTitle", "Edit Outlet");
        return "outlet/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Outlet outlet, RedirectAttributes ra) {
        try {
            outletService.update(id, outlet);
            ra.addFlashAttribute("successMsg", "Outlet updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/outlets";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            outletService.delete(id);
            ra.addFlashAttribute("successMsg", "Outlet deleted!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Cannot delete outlet – it may have linked stock or sales.");
        }
        return "redirect:/outlets";
    }

    @GetMapping("/{id}/stock")
    public String viewStock(@PathVariable Long id, Model model) {
        model.addAttribute("outlet", outletService.findById(id));
        model.addAttribute("stockList", outletService.getOutletStock(id));
        return "outlet/stock";
    }
}
