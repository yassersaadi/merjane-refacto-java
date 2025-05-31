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

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        productRepository.save(p);
        notificationService.sendDelayNotification(leadTime, p.getName());
    }

    public void handleSeasonalProduct(Product p) {
        if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            productRepository.save(p);
        } else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
            notificationService.sendOutOfStockNotification(p.getName());
            productRepository.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
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

}