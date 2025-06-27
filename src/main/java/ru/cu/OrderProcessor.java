package ru.cu;

/*
 * Smell Safari Exercise
 */

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// я бы это вынес в отдельный файл
abstract class BaseOrderProcessor {
    public void beforeProcess() {}
    public void afterProcess() {}
}

// очень забавное использование Closeable -- туда же Override

// Class очень большой
public class OrderProcessor extends BaseOrderProcessor implements Closeable {

    public static final OrderProcessor INSTANCE = new OrderProcessor();

    private static final Logger logger = Logger.getLogger("OrderProcessor");

    private double taxRate = 0.2;

    private String lastCustomerName;
    private double lastGrandTotal;
    private int operationsCounter;

    // Ещё один функционал, который излишен для этого класса
    private final Map<String, Double> loyaltyCache = new HashMap<>();

    private String tempReport;

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
                              int shippingSpeed,
                              boolean sendEmailFlag,
                              boolean sendSmsFlag,
                              boolean sendPushFlag) {

        beforeProcess();

        operationsCounter++;
        logger.info("--- Start processing batch #" + operationsCounter + " ---");

        double total = 0;
        for (Order o : orders) {
            double amount = o.getAmount();

            if (isVip) {
                amount = amount * 0.9;
            }
            if (discountPercent > 0) {
                amount = amount - amount * discountPercent / 100.0;
            }
            total += amount;

            String province = o.getCustomer().getAddress().getProvince();
            if (province != null && province.length() > 10) {
                logger.fine("Long province name: " + province);
            }
        }

        // Считаем что-то с деньгами в даблах
        double shippingCost = calculateShipping(shippingMethod, shippingSpeed);
        double tax = total * taxRate;
        double grandTotal = total + shippingCost + tax;

        if (sendEmailFlag) {
            sendEmail(customerName, addressLine1, addressLine2, postalCode, city, country, grandTotal);
        }
        if (sendSmsFlag) {
            sendSMS(customerName, country, grandTotal);
        }
        if (sendPushFlag) {
            sendPush(customerName, grandTotal);
        }

        logTransaction(customerName, grandTotal);

        double loyalty = grandTotal * 0.05;
        if ("Finland".equalsIgnoreCase(country)) {
            logger.info("Finland loyalty points added: " + loyalty);
        } else {
            logger.info("Non-Finland loyalty points added: " + loyalty);
        }

        tempReport = generateReport(customerName, orders, grandTotal);
        // сомнительно, зачем fine
        logger.fine(tempReport);

        lastCustomerName = customerName;
        lastGrandTotal = grandTotal;

        afterProcess();
    }

    // Непонятный switch с непонятными значениями и магическими числами
    private double calculateShipping(int method, int speed) {
        switch (method) {
            case 1 -> {
                return speed == 1 ? 5 : speed == 2 ? 10 : 20;
            }
            case 2 -> {
                return speed == 1 ? 15 : speed == 2 ? 25 : 35;
            }
            default -> {
                return 10;
            }
        }
    }

    // Вынести бы в отдельную сущность и класс для генерации репорта
    @Deprecated
    public String generateReport(String customerName, List<Order> orders, double grandTotal) {
        StringBuilder sb = new StringBuilder();
        sb.append("Report for ").append(customerName).append("\n");
        for (Order o : orders) {
            sb.append("Order #").append(o.getId())
                    .append(" → ").append(o.getAmount())
                    .append("\n");
        }
        sb.append("TOTAL: ").append(grandTotal).append("\n");
        logger.info("Report generated for " + customerName);
        return sb.toString();
    }

    // не юзается
    public String getCustomerPhone(Order o) {
        return o.getCustomer().getPhone();
    }
    // не юзается
    public String getCustomerEmail(Order o) {
        return o.getCustomer().getEmail();
    }

    // слишком много функционала
    private void sendEmail(String name, String a1, String a2, String pc, String city, String country, double total) {
        try {
            if (total < 0) throw new IOException("Negative total!");
            logger.info("[EMAIL] " + name + " spent " + total);
        } catch (Exception ignored) {
        }
    }

    // слишком много функционала
    private void sendSMS(String name, String country, double total) {
        logger.info("[SMS] Hello, " + name + " from " + country + ": " + total);
    }

    // слишком много функционала
    private void sendPush(String name, double total) {
        logger.info("[PUSH] Hi " + name + ": " + total);
    }

    private void logTransaction(String customer, double total) {
        logger.log(Level.INFO, customer + " spent " + total);
    }

    // не юзается
    public double calculateTotalAmounts(List<Order> orders) {
        double sum = 0;
        for (Order o : orders) {
            sum += o.getAmount();
        }
        return sum;
    }

    // не юзается + ВРЗВРАЩАЕТ 42?)))
    private double oldCalculate(List<Order> orders) {
        return 42;
    }

    @Override
    public void close() {
    }

    // надо вынести
    public static class Order {
        private final String id;
        private final double amount;
        private final Customer customer;

        public Order(String id, double amount, Customer customer) {
            this.id = id;
            this.amount = amount;
            this.customer = customer;
        }
        public String getId() { return id; }
        public double getAmount() { return amount; }
        public Customer getCustomer() { return customer; }
    }

    // надо вынести
    public static class Customer {
        private final String name;
        private final String phone;
        private final String email;
        private final Address address;
        public Customer(String name, String phone, String email, Address address) {
            this.name = name; this.phone = phone; this.email = email; this.address = address;
        }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public Address getAddress() { return address; }
    }

    // надо вынести
    public static class Address {
        private final String line1;
        private final String line2;
        private final String postal;
        private final String city;
        private final String province;
        private final String country;
        public Address(String line1, String line2, String postal, String city, String province, String country) {
            this.line1 = line1; this.line2 = line2; this.postal = postal; this.city = city; this.province = province; this.country = country;
        }
        public String getProvince() { return province; }
    }
}