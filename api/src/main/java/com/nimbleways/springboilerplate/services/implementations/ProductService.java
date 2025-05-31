package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final NotificationService notificationService;


    public ProductService(NotificationService notificationService, ProductRepository productRepository) {
        this.notificationService = notificationService;
        this.productRepository = productRepository;
    }

    public void handleSeasonalProduct(Product product) {
        String productName = product.getName();
        if (LocalDate.now().plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(productName);
            product.setAvailable(0);
        } else if (product.getSeasonStartDate().isAfter(LocalDate.now())) {
            notificationService.sendOutOfStockNotification(productName);
        } else {
            notificationService.sendDelayNotification(product.getLeadTime(), productName);
        }
        productRepository.save(product);
    }

    public void handleExpiredProduct(Product product) {
        product.setAvailable(calculateAvailableProduct(product));
        if (!product.isAvailable()) {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        }
        productRepository.save(product);
    }

    private int calculateAvailableProduct(Product product) {
        if (product.isAvailable() && product.isExpired()) {
            return product.getAvailable() - 1;
        }
        return 0;
    }

    public void handleNormalProduct(Product p) {
        if (p.isAvailable()) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else if (p.hasLeadTime()) {
            Integer leadTime = p.getLeadTime();
            p.setLeadTime(leadTime);
            notificationService.sendDelayNotification(leadTime, p.getName());
            productRepository.save(p);
        }
    }

}