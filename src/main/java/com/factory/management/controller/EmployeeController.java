package com.factory.management.controller;

import com.factory.management.entity.Employee;
import com.factory.management.service.EmployeeService;
import com.factory.management.service.FactoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final FactoryService factoryService;

    public EmployeeController(EmployeeService employeeService, FactoryService factoryService) {
        this.employeeService = employeeService;
        this.factoryService = factoryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("employees", employeeService.search(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("employees", employeeService.findAll());
        }
        model.addAttribute("activeCount", employeeService.countActive());
        return "employee/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("factories", factoryService.findAll());
        model.addAttribute("pageTitle", "Add Employee");
        return "employee/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Employee employee, RedirectAttributes ra) {
        try {
            employeeService.save(employee);
            ra.addFlashAttribute("successMsg", "Employee added successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id);
        if (employee.getUser() != null) {
            employee.setUsername(employee.getUser().getUsername());
        }
        model.addAttribute("employee", employee);
        model.addAttribute("factories", factoryService.findAll());
        model.addAttribute("pageTitle", "Edit Employee");
        return "employee/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Employee employee, RedirectAttributes ra) {
        try {
            employeeService.update(id, employee);
            ra.addFlashAttribute("successMsg", "Employee updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            employeeService.delete(id);
            ra.addFlashAttribute("successMsg", "Employee deleted!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error deleting employee.");
        }
        return "redirect:/employees";
    }
}
