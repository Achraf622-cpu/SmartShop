package org.example.smartshopv2.config;

import lombok.RequiredArgsConstructor;
import org.example.smartshopv2.entity.Product;
import org.example.smartshopv2.entity.User;
import org.example.smartshopv2.enums.Role;
import org.example.smartshopv2.repository.ProductRepository;
import org.example.smartshopv2.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        initializeAdmin();
        initializeProducts();
    }

    private void initializeAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password("admin123")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("✅ Default admin user created: username=admin, password=admin123");
        }
    }

    private void initializeProducts() {
            if (productRepository.count() == 0) {
            List<Product> products = List.of(
                    // Informatique
                    Product.builder()
                            .name("Laptop Dell Latitude 5540")
                            .description(
                                    "Ordinateur portable professionnel 15.6 pouces, Intel Core i7, 16GB RAM, 512GB SSD")
                            .priceHT(8500.00)
                            .stockQuantity(25)
                            .build(),
                    Product.builder()
                            .name("MacBook Pro 14 M3")
                            .description("Apple MacBook Pro 14 pouces, puce M3 Pro, 18GB RAM, 512GB SSD")
                            .priceHT(15000.00)
                            .stockQuantity(15)
                            .build(),
                    Product.builder()
                            .name("Écran Dell UltraSharp 27")
                            .description("Moniteur 27 pouces 4K USB-C, compatible avec station d'accueil")
                            .priceHT(4200.00)
                            .stockQuantity(40)
                            .build(),
                    Product.builder()
                            .name("Clavier Logitech MX Keys")
                            .description("Clavier sans fil rétroéclairé pour professionnels, multi-device")
                            .priceHT(850.00)
                            .stockQuantity(100)
                            .build(),
                    Product.builder()
                            .name("Souris Logitech MX Master 3S")
                            .description("Souris ergonomique sans fil, capteur 8000 DPI, USB-C")
                            .priceHT(750.00)
                            .stockQuantity(80)
                            .build(),

                    // Mobilier de bureau
                    Product.builder()
                            .name("Bureau Assis-Debout Électrique")
                            .description("Bureau réglable en hauteur 160x80cm, motorisé, mémorisation positions")
                            .priceHT(3500.00)
                            .stockQuantity(20)
                            .build(),
                    Product.builder()
                            .name("Chaise Ergonomique Herman Miller")
                            .description("Fauteuil de bureau ergonomique Aeron, support lombaire ajustable")
                            .priceHT(9800.00)
                            .stockQuantity(12)
                            .build(),
                    Product.builder()
                            .name("Lampe de Bureau LED")
                            .description("Lampe LED avec variateur et température de couleur réglable")
                            .priceHT(450.00)
                            .stockQuantity(60)
                            .build(),

                    // Fournitures
                    Product.builder()
                            .name("Ramette Papier A4 (500 feuilles)")
                            .description("Papier blanc 80g/m², qualité premium pour impression")
                            .priceHT(45.00)
                            .stockQuantity(500)
                            .build(),
                    Product.builder()
                            .name("Cartouche Encre HP 963XL Noir")
                            .description("Cartouche haute capacité pour imprimantes HP OfficeJet Pro")
                            .priceHT(320.00)
                            .stockQuantity(150)
                            .build(),
                    Product.builder()
                            .name("Pack Stylos Bic Cristal (50 pcs)")
                            .description("Stylos à bille classiques, encre bleue, pointe moyenne")
                            .priceHT(85.00)
                            .stockQuantity(200)
                            .build(),

                    // Équipement réseau
                    Product.builder()
                            .name("Switch Cisco 24 Ports PoE+")
                            .description("Switch manageable Gigabit avec alimentation PoE+ 370W")
                            .priceHT(6500.00)
                            .stockQuantity(10)
                            .build(),
                    Product.builder()
                            .name("Point d'Accès WiFi 6 Ubiquiti")
                            .description("Access Point UniFi U6-Pro, WiFi 6, PoE, couverture 300m²")
                            .priceHT(1800.00)
                            .stockQuantity(30)
                            .build(),
                    Product.builder()
                            .name("Câble Ethernet Cat6 (100m)")
                            .description("Bobine câble réseau Cat6 blindé, intérieur/extérieur")
                            .priceHT(650.00)
                            .stockQuantity(50)
                            .build(),

                    // Sécurité
                    Product.builder()
                            .name("Caméra IP Hikvision 4MP")
                            .description("Caméra de surveillance PoE, vision nocturne, détection mouvement")
                            .priceHT(1200.00)
                            .stockQuantity(45)
                            .build());

            productRepository.saveAll(products);
            System.out.println("✅ " + products.size() + " products seeded successfully!");
        } else {
            System.out.println("ℹ️ Products already exist, skipping seed.");
        }
    }
}
