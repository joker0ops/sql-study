package com.sqlearn.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Random;

/**
 * 为每个用户的专属练习库生成 8 张相关表与大量示例数据（固定随机种子，保证各用户数据集一致）。
 */
@Component
public class PracticeDataSeeder {

    private static final String[] CATEGORIES = {
            "Electronics", "Clothing", "Books", "Home & Garden", "Sports", "Toys & Games",
            "Beauty", "Food & Beverage", "Automotive", "Health", "Music", "Office",
            "Pets", "Baby", "Jewelry", "Tools", "Outdoor", "Video Games", "Grocery", "Furniture"
    };

    private static final String[] DEPARTMENTS = {
            "Engineering", "Sales", "Marketing", "Human Resources", "Finance", "Operations",
            "Customer Support", "Design", "Legal", "Research", "Information Technology",
            "Logistics", "Purchasing", "Quality Assurance", "Administration",
            "Business Development", "Public Relations", "Training", "Security", "Data"
    };

    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Carol", "David", "Emma", "Frank", "Grace", "Henry", "Ivy", "Jack",
            "Kate", "Leo", "Mia", "Noah", "Olivia", "Paul", "Quinn", "Rose", "Sam", "Tina",
            "Uma", "Victor", "Wendy", "Xavier", "Yara", "Zoe"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Wilson", "Anderson", "Taylor", "Thomas", "Moore",
            "Jackson", "Martin", "Lee", "Perez", "Thompson"
    };

    private static final String[] CITIES = {
            "Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Hangzhou", "Chengdu", "Wuhan",
            "Nanjing", "Xian", "Suzhou", "Tianjin", "Chongqing", "Changsha", "Qingdao", "Xiamen"
    };

    private static final String[] STATUSES = {"pending", "paid", "shipped", "completed", "cancelled"};

    private static final int PRODUCT_COUNT = 200;
    private static final int CUSTOMER_COUNT = 5000;
    private static final int ORDER_COUNT = 8000;
    private static final int EMPLOYEE_COUNT = 300;
    private static final int SUPPLIER_COUNT = 50;

    public void dropAll(Connection conn) throws Exception {
        String[] tables = {"order_items", "orders", "employees", "suppliers", "products",
                "customers", "categories", "departments"};
        try (Statement st = conn.createStatement()) {
            for (String t : tables) {
                st.execute("DROP TABLE IF EXISTS " + t);
            }
        }
    }

    public void seed(Connection conn) throws Exception {
        createTables(conn);
        Random rnd = new Random(42);
        seedCategories(conn);
        double[] productPrices = seedProducts(conn, rnd);
        seedDepartments(conn, rnd);
        seedSuppliers(conn, rnd);
        seedCustomers(conn, rnd);
        seedEmployees(conn, rnd);
        seedOrdersAndItems(conn, rnd, productPrices);
    }

    private void createTables(Connection conn) throws Exception {
        String[] ddl = {
                "CREATE TABLE categories (category_id INT PRIMARY KEY, name VARCHAR(100) NOT NULL)",
                "CREATE TABLE products (product_id INT PRIMARY KEY, name VARCHAR(200) NOT NULL, category_id INT, price DECIMAL(10,2), stock INT)",
                "CREATE TABLE customers (customer_id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, email VARCHAR(200), city VARCHAR(100), signup_date DATE)",
                "CREATE TABLE orders (order_id INT PRIMARY KEY, customer_id INT, order_date DATE, status VARCHAR(50), total_amount DECIMAL(12,2))",
                "CREATE TABLE order_items (item_id INT PRIMARY KEY, order_id INT, product_id INT, quantity INT, unit_price DECIMAL(10,2))",
                "CREATE TABLE departments (department_id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, location VARCHAR(100))",
                "CREATE TABLE employees (employee_id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, department_id INT, salary DECIMAL(10,2), hire_date DATE, manager_id INT)",
                "CREATE TABLE suppliers (supplier_id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, city VARCHAR(100))"
        };
        try (Statement st = conn.createStatement()) {
            for (String s : ddl) {
                st.execute(s);
            }
        }
    }

    private void seedCategories(Connection conn) throws Exception {
        String sql = "INSERT INTO categories(category_id, name) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                ps.setInt(1, i + 1);
                ps.setString(2, CATEGORIES[i]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private double[] seedProducts(Connection conn, Random rnd) throws Exception {
        double[] prices = new double[PRODUCT_COUNT + 1];
        String sql = "INSERT INTO products(product_id, name, category_id, price, stock) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= PRODUCT_COUNT; i++) {
                int categoryId = 1 + rnd.nextInt(CATEGORIES.length);
                String name = CATEGORIES[categoryId - 1] + " item " + i;
                double price = round2(5 + rnd.nextInt(5000));
                int stock = rnd.nextInt(1000);
                prices[i] = price;
                ps.setInt(1, i);
                ps.setString(2, name);
                ps.setInt(3, categoryId);
                ps.setBigDecimal(4, dec(price));
                ps.setInt(5, stock);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return prices;
    }

    private void seedDepartments(Connection conn, Random rnd) throws Exception {
        String sql = "INSERT INTO departments(department_id, name, location) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < DEPARTMENTS.length; i++) {
                ps.setInt(1, i + 1);
                ps.setString(2, DEPARTMENTS[i]);
                ps.setString(3, CITIES[rnd.nextInt(CITIES.length)]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void seedSuppliers(Connection conn, Random rnd) throws Exception {
        String sql = "INSERT INTO suppliers(supplier_id, name, city) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= SUPPLIER_COUNT; i++) {
                ps.setInt(1, i);
                ps.setString(2, "Supplier " + i);
                ps.setString(3, CITIES[rnd.nextInt(CITIES.length)]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void seedCustomers(Connection conn, Random rnd) throws Exception {
        String sql = "INSERT INTO customers(customer_id, name, email, city, signup_date) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= CUSTOMER_COUNT; i++) {
                String first = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)];
                String last = LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];
                ps.setInt(1, i);
                ps.setString(2, first + " " + last);
                ps.setString(3, first.toLowerCase() + "." + last.toLowerCase() + i + "@example.com");
                ps.setString(4, CITIES[rnd.nextInt(CITIES.length)]);
                ps.setDate(5, Date.valueOf(LocalDate.now().minusDays(rnd.nextInt(1095))));
                ps.addBatch();
                if (i % 1000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void seedEmployees(Connection conn, Random rnd) throws Exception {
        String sql = "INSERT INTO employees(employee_id, name, department_id, salary, hire_date, manager_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= EMPLOYEE_COUNT; i++) {
                String name = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];
                ps.setInt(1, i);
                ps.setString(2, name);
                ps.setInt(3, 1 + rnd.nextInt(DEPARTMENTS.length));
                ps.setBigDecimal(4, dec(4000 + rnd.nextInt(46000)));
                ps.setDate(5, Date.valueOf(LocalDate.now().minusDays(rnd.nextInt(3650))));
                if (i > 1 && rnd.nextInt(100) >= 15) {
                    ps.setInt(6, 1 + rnd.nextInt(i - 1));
                } else {
                    ps.setNull(6, java.sql.Types.INTEGER);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void seedOrdersAndItems(Connection conn, Random rnd, double[] productPrices) throws Exception {
        String orderSql = "INSERT INTO orders(order_id, customer_id, order_date, status, total_amount) VALUES (?,?,?,?,?)";
        String itemSql = "INSERT INTO order_items(item_id, order_id, product_id, quantity, unit_price) VALUES (?,?,?,?,?)";
        try (PreparedStatement orderPs = conn.prepareStatement(orderSql);
             PreparedStatement itemPs = conn.prepareStatement(itemSql)) {
            int itemId = 0;
            for (int o = 1; o <= ORDER_COUNT; o++) {
                int itemCount = 1 + rnd.nextInt(7);
                double total = 0;
                for (int k = 0; k < itemCount; k++) {
                    itemId++;
                    int productId = 1 + rnd.nextInt(PRODUCT_COUNT);
                    int quantity = 1 + rnd.nextInt(10);
                    double unitPrice = productPrices[productId];
                    total += quantity * unitPrice;
                    itemPs.setInt(1, itemId);
                    itemPs.setInt(2, o);
                    itemPs.setInt(3, productId);
                    itemPs.setInt(4, quantity);
                    itemPs.setBigDecimal(5, dec(unitPrice));
                    itemPs.addBatch();
                }
                orderPs.setInt(1, o);
                orderPs.setInt(2, 1 + rnd.nextInt(CUSTOMER_COUNT));
                orderPs.setDate(3, Date.valueOf(LocalDate.now().minusDays(rnd.nextInt(730))));
                orderPs.setString(4, STATUSES[rnd.nextInt(STATUSES.length)]);
                orderPs.setBigDecimal(5, dec(total));
                orderPs.addBatch();
                if (o % 500 == 0) {
                    orderPs.executeBatch();
                    itemPs.executeBatch();
                }
            }
            orderPs.executeBatch();
            itemPs.executeBatch();
        }
    }

    private static BigDecimal dec(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static double round2(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
