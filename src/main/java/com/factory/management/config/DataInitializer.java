package com.factory.management.config;

import com.factory.management.entity.*;
import com.factory.management.repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FactoryStockRepository factoryStockRepository;
    private final OutletRepository outletRepository;
    private final EmployeeRepository employeeRepository;
    private final FactoryRepository factoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           ProductRepository productRepository, FactoryStockRepository factoryStockRepository,
                           OutletRepository outletRepository, EmployeeRepository employeeRepository,
                           FactoryRepository factoryRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.factoryStockRepository = factoryStockRepository;
        this.outletRepository = outletRepository;
        this.employeeRepository = employeeRepository;
        this.factoryRepository = factoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // Set up the basic security roles if they don't exist yet
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role cashierRole = roleRepository.findByName("ROLE_CASHIER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_CASHIER")));

        // Create default accounts for testing and initial setup
        User admin = userRepository.findByUsername("admin").orElseGet(User::new);
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Administrator");
        admin.setEmail("admin@factory.com");
        admin.setEnabled(true);
        admin.setRole(adminRole);
        userRepository.save(admin);

        User cashier = userRepository.findByUsername("cashier1").orElseGet(User::new);
        cashier.setUsername("cashier1");
        cashier.setPassword(passwordEncoder.encode("cashier123"));
        cashier.setFullName("Main Cashier");
        cashier.setEmail("cashier@factory.com");
        cashier.setEnabled(true);
        cashier.setRole(cashierRole);
        userRepository.save(cashier);

        // Add some initial factory locations
        Factory factory1 = null;
        Factory factory2 = null;
        if (factoryRepository.count() == 0) {
            factory1 = new Factory();
            factory1.setName("Colombo Textile Factory");
            factory1.setLocation("Industrial Zone, Colombo 15");
            factory1.setPhone("0112-789012");
            factory1 = factoryRepository.save(factory1);

            factory2 = new Factory();
            factory2.setName("Kandy Production Center");
            factory2.setLocation("Peradeniya Industrial Estate, Kandy");
            factory2.setPhone("0812-445566");
            factory2 = factoryRepository.save(factory2);
        } else {
            java.util.List<Factory> existingFactories = factoryRepository.findAll();
            if (!existingFactories.isEmpty()) {
                factory1 = existingFactories.get(0);
                if (existingFactories.size() > 1) {
                    factory2 = existingFactories.get(1);
                } else {
                    factory2 = factory1;
                }
            }
        }

        // Clean up any old constraints that might interfere with multi-factory stock
        try {
            factoryStockRepository.dropOldUniqueConstraint();
        } catch (Exception e) {
            // Already dropped or doesn't exist, no big deal
        }

        // Run some migration logic to fix invalid stock records if they exist
        if (factory1 != null) {
            try {
                factoryStockRepository.migrateInvalidFactoryStocks(factory1.getId());
            } catch (Exception e) {
                try {
                    factoryStockRepository.deleteInvalidFactoryStocks();
                } catch (Exception ex) {
                    System.err.println("Could not clean invalid factory stocks: " + ex.getMessage());
                }
            }
        } else {
            try {
                factoryStockRepository.deleteInvalidFactoryStocks();
            } catch (Exception ex) {
                System.err.println("Could not clean invalid factory stocks: " + ex.getMessage());
            }
        }

        // Populate seed data for products and distribute them across factories
        if (productRepository.count() == 0 && factory1 != null && factory2 != null) {
            String[][] products = {
                {"Cotton T-Shirt", "Premium quality cotton t-shirt", "850.00", "Clothing"},
                {"Denim Jeans", "Classic fit denim jeans", "2500.00", "Clothing"},
                {"Leather Belt", "Genuine leather belt", "650.00", "Accessories"},
                {"Canvas Sneakers", "Lightweight canvas sneakers", "1800.00", "Footwear"},
                {"Hooded Jacket", "Warm hooded jacket", "3200.00", "Clothing"},
                {"Polo Shirt", "Casual polo shirt", "1200.00", "Clothing"},
                {"Sports Shorts", "Breathable sports shorts", "700.00", "Clothing"},
                {"Backpack", "Durable everyday backpack", "2800.00", "Accessories"}
            };

            int[] factory1Quantities = {100, 50, 120, 40, 30, 80, 60, 20};
            int[] factory2Quantities = {50, 30, 80, 20, 15, 40, 30, 15};
            int[] thresholds = {20, 10, 30, 10, 8, 15, 12, 5};

            for (int i = 0; i < products.length; i++) {
                Product p = new Product();
                p.setName(products[i][0]);
                p.setDescription(products[i][1]);
                p.setUnitPrice(new BigDecimal(products[i][2]));
                p.setCategory(products[i][3]);
                Product saved = productRepository.save(p);

                // Stock in Factory 1
                FactoryStock fs1 = new FactoryStock();
                fs1.setFactory(factory1);
                fs1.setProduct(saved);
                fs1.setQuantity(factory1Quantities[i]);
                fs1.setLowStockThreshold(thresholds[i]);
                factoryStockRepository.save(fs1);

                // Stock in Factory 2
                FactoryStock fs2 = new FactoryStock();
                fs2.setFactory(factory2);
                fs2.setProduct(saved);
                fs2.setQuantity(factory2Quantities[i]);
                fs2.setLowStockThreshold(thresholds[i]);
                factoryStockRepository.save(fs2);
            }
        }

        // Self-healing: make sure every product has a stock record for every factory
        java.util.List<Product> allProducts = productRepository.findAll();
        java.util.List<Factory> allFactories = factoryRepository.findAll();
        for (Product product : allProducts) {
            for (Factory factory : allFactories) {
                if (factoryStockRepository.findByFactoryIdAndProductId(factory.getId(), product.getId()).isEmpty()) {
                    FactoryStock stock = new FactoryStock();
                    stock.setFactory(factory);
                    stock.setProduct(product);
                    stock.setQuantity(0);
                    stock.setLowStockThreshold(10);
                    stock.setUpdatedAt(java.time.LocalDateTime.now());
                    factoryStockRepository.save(stock);
                }
            }
        }

        // Seed some initial outlet branches
        if (outletRepository.count() == 0) {
            String[][] outlets = {
                {"Colombo Main Outlet", "45 Galle Road, Colombo 03", "0112-345678"},
                {"Kandy Branch", "12 Peradeniya Road, Kandy", "0812-223344"},
                {"Galle Showroom", "78 Matara Road, Galle", "0912-556677"}
            };

            for (String[] o : outlets) {
                Outlet outlet = new Outlet();
                outlet.setName(o[0]);
                outlet.setLocation(o[1]);
                outlet.setPhone(o[2]);
                outletRepository.save(outlet);
            }
        }

        // Add some dummy employee records for the list view
        if (employeeRepository.count() == 0) {
            Object[][] employees = {
                {"Kamal Perera", "199012345678", "0771234567", "kamal@factory.com", "Production Manager", "Production", new BigDecimal("75000"), LocalDate.of(2020, 1, 15)},
                {"Nimal Silva", "198509876543", "0779876543", "nimal@factory.com", "Quality Control", "Production", new BigDecimal("55000"), LocalDate.of(2021, 3, 1)},
                {"Sunethra Fernando", "199312341234", "0762345678", "sunethra@factory.com", "Cashier", "Sales", new BigDecimal("45000"), LocalDate.of(2022, 6, 10)},
                {"Roshan Jayaweera", "199001230123", "0754321234", "roshan@factory.com", "Store Keeper", "Warehouse", new BigDecimal("48000"), LocalDate.of(2021, 9, 5)},
                {"Chamari Bandara", "199512346789", "0776789012", "chamari@factory.com", "HR Officer", "HR", new BigDecimal("60000"), LocalDate.of(2020, 11, 20)}
            };

            for (Object[] e : employees) {
                Employee emp = new Employee();
                emp.setName((String) e[0]);
                emp.setNic((String) e[1]);
                emp.setPhone((String) e[2]);
                emp.setEmail((String) e[3]);
                emp.setPosition((String) e[4]);
                emp.setDepartment((String) e[5]);
                emp.setSalary((BigDecimal) e[6]);
                emp.setHireDate((LocalDate) e[7]);
                employeeRepository.save(emp);
            }
        }
    }
}
