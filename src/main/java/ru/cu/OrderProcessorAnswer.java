package ru.cu;

/*
 * Smell Safari Exercise
 * ---------------------
 * Найдите как можно больше запахов кода.
 * (Long Method, Long Parameter List, Primitive Obsession, Duplicate Code,
 *  Feature Envy, Switch Statements, Large Class, etc.)
 */

import java.util.List;
import java.util.logging.Logger;

public class OrderProcessorAnswer {

    private static final Logger logger = Logger.getLogger("OrderProcessor");

    // Magic number obsession
    private double taxRate = 0.2;

    // Too many responsibilities + state
    private String lastCustomerName;
    private double lastGrandTotal;
    private int operationsCounter;

    public void processOrders(List<Order> orders,
                              String customerName,
                              String addressLine1,
                              String addressLine2,
                              String postalCode,
                              String city,
                              String country,
                              boolean isVip,
                              int discountPercent,
                              int customerGroup,
                              int shippingMethod,
                              int shippingSpeed) {

        operationsCounter++;

        // -------- LONG METHOD STARTS --------
        double total = 0;
        for (Order o : orders) {
            double amount = o.getAmount();

            // Duplicate discount logic
            if (isVip) {
                amount = amount * 0.9;
            }
            if (discountPercent > 0) {
                amount = amount - amount * discountPercent / 100.0;
            }
            total += amount;
        }

        // Shipping cost calculation via switch
        double shippingCost;
        switch (shippingMethod) {
            case 1:
                shippingCost = calculateDomesticShipping(shippingSpeed);
                break;
            case 2:
                shippingCost = calculateInternationalShipping(shippingSpeed);
                break;
            default:
                shippingCost = 10; // Magic number
        }

        double tax = total * taxRate;
        double grandTotal = total + shippingCost + tax;

        sendEmail(customerName, addressLine1, addressLine2, postalCode, city, country, grandTotal);
        logTransaction(customerName, grandTotal);

        // Duplicate loyalty calculation
        if (country.equalsIgnoreCase("Finland")) {
            double loyalty = grandTotal * 0.05;
            logger.info("Finland loyalty points added: " + loyalty);
        } else {
            double loyalty = grandTotal * 0.05;
            logger.info("Non-Finland loyalty points added: " + loyalty);
        }
        // -------- LONG METHOD ENDS --------

        lastCustomerName = customerName;
        lastGrandTotal = grandTotal;
    }

    private double calculateDomesticShipping(int speed) {
        if (speed == 1) {
            return 5;
        } else if (speed == 2) {
            return 10;
        } else {
            return 20;
        }
    }

    private double calculateInternationalShipping(int speed) {
        if (speed == 1) {
            return 15;
        } else if (speed == 2) {
            return 25;
        } else {
            return 35;
        }
    }

    private void sendEmail(String name, String a1, String a2, String pc, String city, String country, double total) {
        // Pretend email sending
    }

    private void logTransaction(String customer, double total) {
        logger.info(customer + " spent " + total);
    }

    // Feature Envy example: this method is arguably Order's responsibility
    public double calculateTotalAmounts(List<Order> orders) {
        double sum = 0;
        for (Order o : orders) {
            sum += o.getAmount(); // Envying Order's data
        }
        return sum;
    }

    // Nested placeholder class to keep example self‑contained
    public static class Order {
        private final String id;
        private final double amount;

        public Order(String id, double amount) {
            this.id = id;
            this.amount = amount;
        }

        public String getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }
    }
}
