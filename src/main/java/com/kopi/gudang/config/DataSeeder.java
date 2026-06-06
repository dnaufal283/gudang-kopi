package com.kopi.gudang.config;

import com.kopi.gudang.entity.Product;
import com.kopi.gudang.entity.StockTransaction;
import com.kopi.gudang.entity.User;
import com.kopi.gudang.repository.ProductRepository;
import com.kopi.gudang.repository.StockTransactionRepository;
import com.kopi.gudang.repository.UserRepository;
import com.kopi.gudang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockTransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Default Users
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // Will be hashed inside registerUser
            admin.setFullName("Administrator Gudang");
            admin.setRole("ADMIN");
            userService.registerUser(admin);

            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword("staff123");
            staff.setFullName("Staff Warehouse");
            staff.setRole("STAFF");
            userService.registerUser(staff);

            System.out.println(">>> Seeded default users: admin/admin123 and staff/staff123");
        }

        // 2. Seed Default Products and Transactions
        if (productRepository.count() == 0) {
            // Product 1
            Product p1 = new Product();
            p1.setName("Gayo Washed");
            p1.setType("Arabika");
            p1.setStock(50);
            p1.setPrice(120000.0);
            p1 = productRepository.save(p1);

            // Transaction for Product 1
            StockTransaction t1 = new StockTransaction();
            t1.setProduct(p1);
            t1.setType("IN");
            t1.setQuantity(50);
            t1.setNotes("Penerimaan awal supplier Gayo");
            t1.setTransactionDate(LocalDateTime.now().minusDays(5));
            t1.setPerformedBy("system");
            transactionRepository.save(t1);

            // Product 2
            Product p2 = new Product();
            p2.setName("Toraja Kalosi");
            p2.setType("Arabika");
            p2.setStock(8); // Low stock (threshold < 10)
            p2.setPrice(135000.0);
            p2 = productRepository.save(p2);

            // Transactions for Product 2 (IN then OUT)
            StockTransaction t2_in = new StockTransaction();
            t2_in.setProduct(p2);
            t2_in.setType("IN");
            t2_in.setQuantity(10);
            t2_in.setNotes("Penerimaan awal supplier Toraja");
            t2_in.setTransactionDate(LocalDateTime.now().minusDays(3));
            t2_in.setPerformedBy("system");
            transactionRepository.save(t2_in);

            StockTransaction t2_out = new StockTransaction();
            t2_out.setProduct(p2);
            t2_out.setType("OUT");
            t2_out.setQuantity(2);
            t2_out.setNotes("Pengiriman ke Outlet A");
            t2_out.setTransactionDate(LocalDateTime.now().minusDays(1));
            t2_out.setPerformedBy("admin");
            t2_out.setInvoiceNumber("INV-20260601000001");
            transactionRepository.save(t2_out);

            // Product 3
            Product p3 = new Product();
            p3.setName("Mandheling Natural");
            p3.setType("Arabika");
            p3.setStock(25);
            p3.setPrice(140000.0);
            p3 = productRepository.save(p3);

            StockTransaction t3 = new StockTransaction();
            t3.setProduct(p3);
            t3.setType("IN");
            t3.setQuantity(25);
            t3.setNotes("Penerimaan awal supplier Mandheling");
            t3.setTransactionDate(LocalDateTime.now().minusDays(2));
            t3.setPerformedBy("system");
            transactionRepository.save(t3);

            // Product 4
            Product p4 = new Product();
            p4.setName("Papua Wamena Honey");
            p4.setType("Arabika");
            p4.setStock(5); // Low stock
            p4.setPrice(150000.0);
            p4 = productRepository.save(p4);

            StockTransaction t4 = new StockTransaction();
            t4.setProduct(p4);
            t4.setType("IN");
            t4.setQuantity(5);
            t4.setNotes("Penerimaan awal supplier Papua");
            t4.setTransactionDate(LocalDateTime.now().minusHours(12));
            t4.setPerformedBy("system");
            transactionRepository.save(t4);

            System.out.println(">>> Seeded default products and transactions");
        }
    }
}
