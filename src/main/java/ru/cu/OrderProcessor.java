// неправильное название мейн пакеджа
package ru.cu;

/*
 * Smell Safari Exercise
 */

import java.io.Closeable;
import java.io.IOException;
// анюзед импорт
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class BaseOrderProcessor {
    public void beforeProcess() {}
    public void afterProcess() {}
}

public class OrderProcessor extends BaseOrderProcessor implements Closeable {

    // неиспользуемый объект
    public static final OrderProcessor INSTANCE = new OrderProcessor();

    private static final Logger logger = Logger.getLogger("OrderProcessor");

    // должно быть финальным
    private double taxRate = 0.2;

    // анюзед
    private String lastCustomerName;
    private double lastGrandTotal;
    // конутер в каком скопе? чего? Мега непонятная штука
    private int operationsCounter;

    // анюзед
    private final Map<String, Double> loyaltyCache = new HashMap<>();

    // максимально непонятная строка и нахрен она тут нужна
    private String tempReport;

    // слишком дохрена аргументов, го выносить в дто
    // слишком огромный и нетривиальный метод, надо декомпозировать
    public void processOrders(List<Order> orders,
                              String customerName,
                              String addressLine1,
                              String addressLine2,
                              String postalCode,
                              String city,
                              String country,
                              boolean isVip,
                              int discountPercent,
                              // анюзед аргумент
                              int customerGroup,
                              int shippingMethod,
                              int shippingSpeed,
                              // слишком много флагов - бесполезная херня
                              boolean sendEmailFlag,
                              boolean sendSmsFlag,
                              boolean sendPushFlag) {

        beforeProcess();

        operationsCounter++;
        logger.info("--- Start processing batch #" + operationsCounter + " ---");

        // use BigDecimal
        double total = 0;
        for (Order o : orders) {
            double amount = o.getAmount();

            // нетривиальная логика, выносим в отдельной метод
            if (isVip) {
                amount = amount * 0.9;
            }
            if (discountPercent > 0) {
                amount = amount - amount * discountPercent / 100.0;
            }
            total += amount;

            String province = o.getCustomer().getAddress().getProvince();
            if (province != null && province.length() > 10) {
                // я б не юзал файн)
                logger.fine("Long province name: " + province);
            }
        }

        double shippingCost = calculateShipping(shippingMethod, shippingSpeed);
        double tax = total * taxRate;
        double grandTotal = total + shippingCost + tax;

        // слишком дохрена ифов
        if (sendEmailFlag) {
            sendEmail(customerName, addressLine1, addressLine2, postalCode, city, country, grandTotal);
        }
        if (sendSmsFlag) {
            sendSMS(customerName, country, grandTotal);
        }
        if (sendPushFlag) {
            sendPush(customerName, grandTotal);
        }

        // эт вообще прикол какой то)
        logTransaction(customerName, grandTotal);

        double loyalty = grandTotal * 0.05;
        if ("Finland".equalsIgnoreCase(country)) {
            logger.info("Finland loyalty points added: " + loyalty);
        } else {
            logger.info("Non-Finland loyalty points added: " + loyalty);
        }

        tempReport = generateReport(customerName, orders, grandTotal);
        logger.fine(tempReport);

        // максимальный антипаттерн проектировки
        lastCustomerName = customerName;
        lastGrandTotal = grandTotal;

        afterProcess();
    }

    // метод - инт) Бред
    private double calculateShipping(int method, int speed) {
        switch (method) {
            // максимально нетривиальные кейсы
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

    @Deprecated
    // максимально нетривиальная генерация репорта, надо декомпозировать
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

    // анюзед
    public String getCustomerPhone(Order o) {
        return o.getCustomer().getPhone();
    }
    public String getCustomerEmail(Order o) {
        return o.getCustomer().getEmail();
    }

    // анюзед аргументы
    private void sendEmail(String name, String a1, String a2, String pc, String city, String country, double total) {
        try {
            // думаю точно не IOException, больше тянет на IllegalArgemunt
            if (total < 0) throw new IOException("Negative total!");
            logger.info("[EMAIL] " + name + " spent " + total);
        } catch (Exception ignored) {
        }
    }

    private void sendSMS(String name, String country, double total) {
        logger.info("[SMS] Hello, " + name + " from " + country + ": " + total);
    }

    // ну пойдет
    private void sendPush(String name, double total) {
        logger.info("[PUSH] Hi " + name + ": " + total);
    }

    private void logTransaction(String customer, double total) {
        logger.log(Level.INFO, customer + " spent " + total);
    }

    // анюзед
    public double calculateTotalAmounts(List<Order> orders) {
        // нууу, явно можно написать проще и в 1 строку)
        double sum = 0;
        for (Order o : orders) {
            sum += o.getAmount();
        }
        return sum;
    }

    // анюзед
    private double oldCalculate(List<Order> orders) {
        return 42;
    }

    // норм методы пишем)
    @Override
    public void close() {
    }

    public static class Order {
        private final String id;
        private final double amount;
        // скорее у кастомера должен быть список ордеров, в общем надо думать
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

    public static class Customer {
        private final String name;
        private final String phone;
        private final String email;
        private final Address address;
        public Customer(String name, String phone, String email, Address address) {
            this.name = name; this.phone = phone; this.email = email; this.address = address;
        }
        // анюзед
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public Address getAddress() { return address; }
    }

    public static class Address {
        // анюзед
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
