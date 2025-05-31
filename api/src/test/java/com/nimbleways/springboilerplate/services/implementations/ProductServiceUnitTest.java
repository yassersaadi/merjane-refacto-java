package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
class ProductServiceUnitTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    @Test
    void test() {
        // GIVEN
        Product product = new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.notifyDelay(product.getLeadTime(), product);

        // THEN
        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendDelayNotification(product.getLeadTime(), product.getName());
    }


    @Test
    void handle_expired_product_should_remove_product_when_product_is_available_and_is_expired() {
        Product product = new Product(null,
                15,
                1,
                "NORMAL",
                "RJ45 Cable",
                LocalDate.now().plusDays(1),
                null,
                null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertThat(product.getAvailable()).isZero();
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
    }


    @Test
    void handle_expired_product_should_send_expiration_notification_when_product_is_not_available_and_is_expired() {
        Product product = new Product(null,
                15,
                0,
                "NORMAL",
                "RJ45 Cable",
                LocalDate.now().plusDays(1),
                null,
                null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertThat(product.getAvailable()).isZero();
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendExpirationNotification(product.getName(), product.getExpiryDate());
    }


    @Test
    void handle_expired_product_should_send_expiration_notification_when_product_is_available_and_is_not_expired() {
        Product product = new Product(null,
                15,
                0,
                "NORMAL",
                "RJ45 Cable",
                LocalDate.now().minusDays(1),
                null,
                null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertThat(product.getAvailable()).isZero();
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendExpirationNotification(product.getName(), product.getExpiryDate());
    }


    @Test
    void handle_seasonal_product_should_send_out_of_stock_notification_when_product_will_not_be_available_before_the_end_of_the_season() {
        Product product = new Product(null,
                10,
                10,
                "NORMAL",
                "RJ45 Cable",
                null,
                null,
                LocalDate.now().plusDays(9));


        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);
        assertThat(product.getAvailable()).isZero();
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendOutOfStockNotification(product.getName());
    }

    @Test
    void handle_seasonal_product_should_send_out_of_stock_notification_when_product_season_start_date_is_after_today_date() {
        Product product = new Product(null,
                10,
                10,
                "NORMAL",
                "RJ45 Cable",
                null,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10));


        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);
        assertThat(product.getAvailable()).isEqualTo(10);
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendOutOfStockNotification(product.getName());
    }

    @Test
    void handle_seasonal_product_should_send_delay_notification_when_seasonal_product_is_available() {
        Product product = new Product(null,
                10,
                10,
                "NORMAL",
                "RJ45 Cable",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10));


        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);
        assertThat(product.getAvailable()).isEqualTo(10);
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendDelayNotification(product.getLeadTime(), product.getName());
    }
}