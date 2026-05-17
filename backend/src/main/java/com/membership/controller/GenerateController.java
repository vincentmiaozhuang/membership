package com.membership.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/generate")
public class GenerateController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Random random = new Random();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成充值记录
     * @param count 生成记录数量
     * @param date 指定的日期，格式：yyyy-MM-dd
     * @return 生成结果
     */
    @GetMapping("/recharge-records")
    public String generateRechargeRecords(
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(required = false) String date) {

        try {
            // 获取已有的产品、客户、供应商数据
            List<Product> products = getProducts();
            List<Customer> customers = getCustomers();
            List<Supplier> suppliers = getSuppliers();

            if (products.isEmpty() || customers.isEmpty() || suppliers.isEmpty()) {
                return "Error: No data found in products, customers or suppliers table";
            }

            System.out.printf("Found %d products, %d customers, %d suppliers%n",
                    products.size(), customers.size(), suppliers.size());

            // 生成充值记录
            generateRecords(products, customers, suppliers, count, date);

            return String.format("Successfully generated %d recharge records", count);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating recharge records: " + e.getMessage();
        }
    }

    private List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, face_value FROM products WHERE enabled = true";
        jdbcTemplate.query(sql, rs -> {
            Product product = new Product();
            product.id = rs.getLong("id");
            product.name = rs.getString("name");
            product.facePrice = rs.getBigDecimal("face_value");
            products.add(product);
        });
        return products;
    }

    private List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT id, name FROM customers WHERE enabled = true";
        jdbcTemplate.query(sql, rs -> {
            Customer customer = new Customer();
            customer.id = rs.getLong("id");
            customer.name = rs.getString("name");
            customers.add(customer);
        });
        return customers;
    }

    private List<Supplier> getSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id, name FROM suppliers WHERE enabled = true";
        jdbcTemplate.query(sql, rs -> {
            Supplier supplier = new Supplier();
            supplier.id = rs.getLong("id");
            supplier.name = rs.getString("name");
            suppliers.add(supplier);
        });
        return suppliers;
    }

    private void generateRecords(List<Product> products, 
                               List<Customer> customers, 
                               List<Supplier> suppliers, 
                               int count, 
                               String dateStr) throws ParseException {

        // 计算日期
        Date recordDate;
        if (dateStr != null && !dateStr.isEmpty()) {
            // 使用指定的日期
            recordDate = sdf.parse(dateStr + " 00:00:00");
        } else {
            // 默认使用昨天的日期
            recordDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        }

        String sql = "INSERT INTO recharge_records " +
                "(product_id, product_name, product_face_price, recharge_phone, " +
                "customer_order_id, platform_order_id, customer_id, customer_name, " +
                "customer_price, supplier_id, supplier_name, supplier_price, " +
                "status, description, created_at, updated_at, recharge_person) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // 准备批量更新的数据
        List<Object[]> batchArgs = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // 随机选择产品、客户、供应商
            Product product = products.get(random.nextInt(products.size()));
            Customer customer = customers.get(random.nextInt(customers.size()));
            Supplier supplier = suppliers.get(random.nextInt(suppliers.size()));

            // 随机生成价格
            double facePrice = product.facePrice.doubleValue();
            double customerPrice = facePrice * (1 + (random.nextDouble() * 0.2 - 0.1)); // 上下浮动10%
            double supplierPrice = facePrice * (1 + (random.nextDouble() * 0.1 - 0.05)); // 上下浮动5%

            // 随机状态
            String[] statuses = {"成功", "失败", "充值中"};
            String status = statuses[random.nextInt(statuses.length)];

            // 生成订单ID
            String customerOrderId = generateRandomString(16);
            String platformOrderId = generateRandomString(20);

            // 生成随机手机号
            String rechargePhone = "1" + (1000000000 + random.nextInt(900000000)); // 9位数字

            // 生成指定日期的随机时间
            Date recordDateTime = new Date(recordDate.getTime() + random.nextInt(24 * 60 * 60 * 1000));
            String formattedDate = sdf.format(recordDateTime);

            // 准备参数
            Object[] args = new Object[]{
                product.id,
                product.name,
                product.facePrice,
                rechargePhone,
                customerOrderId,
                platformOrderId,
                customer.id,
                customer.name,
                customerPrice,
                supplier.id,
                supplier.name,
                supplierPrice,
                status,
                "Test recharge record " + (i + 1),
                formattedDate,
                formattedDate, // updated_at
                0L // 系统充值
            };
            batchArgs.add(args);

            // 每10条打印一次
            if ((i + 1) % 10 == 0) {
                System.out.printf("Generated %d records%n", i + 1);
            }
        }
        
        // 执行批量更新
        jdbcTemplate.batchUpdate(sql, batchArgs);

        System.out.printf("Successfully generated %d recharge records%n", count);
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    static class Product {
        long id;
        String name;
        BigDecimal facePrice;
    }

    static class Customer {
        long id;
        String name;
    }

    static class Supplier {
        long id;
        String name;
    }
}
