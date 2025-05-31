package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.util.Set;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final NotificationService notificationService;

    private final OrderRepository orderRepository;

    public ProductService(NotificationService notificationService, ProductRepository productRepository, OrderRepository orderRepository) {
        this.notificationService = notificationService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
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
            notificationService.sendDelayNotification(p.getLeadTime(), p.getName());
            productRepository.save(p);
        }
    }

    public Order proccessOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        Set<Product> products = order.getItems();
        for (Product product : products) {
            if ("NORMAL".equals(product.getType())) {
                handleNormalProduct(product);
            } else if ("SEASONAL".equals(product.getType())) {
                handleSeasonalProduct(product);
            } else if ("EXPIRABLE".equals(product.getType())) {
                handleExpiredProduct(product);
            }
        }
        return order;
    }
}