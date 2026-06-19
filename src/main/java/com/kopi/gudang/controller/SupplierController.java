package com.kopi.gudang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kopi.gudang.entity.Supplier;
import com.kopi.gudang.repository.SupplierRepository;

@Controller
public class SupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    // Menampilkan halaman Kelola Supplier
    @GetMapping("/suppliers")
    public String viewSuppliers(Model model) {
        model.addAttribute("suppliers", supplierRepository.findAll());
        return "suppliers"; // Akan memanggil file suppliers.html
    }

    // Menyimpan data Supplier baru dari form
    @PostMapping("/suppliers")
    public String addSupplier(
            @RequestParam("name") String name,
            @RequestParam("contact") String contact,
            @RequestParam("address") String address) {

        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContact(contact);
        supplier.setAddress(address);

        supplierRepository.save(supplier);
        return "redirect:/suppliers";
    }
}