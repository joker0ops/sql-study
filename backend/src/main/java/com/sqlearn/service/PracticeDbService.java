package com.sqlearn.service;

import com.sqlearn.dto.ColumnInfo;
import com.sqlearn.dto.SqlResult;
import com.sqlearn.dto.TableRelation;
import com.sqlearn.dto.TableSchema;
import com.sqlearn.exception.BizException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理每用户独立的 H2 练习数据库：懒加载数据源、建库/重置/读结构/执行 SQL。
 */
@Service
public class PracticeDbService {

    private static final String[] TABLES = {
            "categories", "products", "customers", "orders",
            "order_items", "departments", "employees", "suppliers"
    };

    private final Map<Long, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    private final String dbDir;
    private final PracticeDataSeeder seeder;

    public PracticeDbService(@Value("${app.practice.db-dir:./data/practice}") String dbDir,
                             PracticeDataSeeder seeder) {
        this.dbDir = dbDir;
        this.seeder = seeder;
    }

    public boolean isInitialized(Long userId) {
        try (Connection conn = getDataSource(userId).getConnection()) {
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "CUSTOMERS", null)) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void initDatabase(Long userId) {
        if (isInitialized(userId)) {
            return;
        }
        try (Connection conn = getDataSource(userId).getConnection()) {
            seeder.seed(conn);
        } catch (Exception e) {
            throw new BizException("初始化专属数据库失败: " + e.getMessage());
        }
    }

    public synchronized void resetDatabase(Long userId) {
        try (Connection conn = getDataSource(userId).getConnection()) {
            seeder.dropAll(conn);
            seeder.seed(conn);
        } catch (Exception e) {
            throw new BizException("重置专属数据库失败: " + e.getMessage());
        }
    }

    public List<TableSchema> getSchema(Long userId) {
        List<TableSchema> schemas = new ArrayList<>();
        try (Connection conn = getDataSource(userId).getConnection()) {
            for (String table : TABLES) {
                List<ColumnInfo> columns = new ArrayList<>();
                try (ResultSet rs = conn.getMetaData().getColumns(null, null, table.toUpperCase(), null)) {
                    while (rs.next()) {
                        String name = rs.getString("COLUMN_NAME").toLowerCase();
                        String type = friendlyType(rs.getString("TYPE_NAME"));
                        columns.add(new ColumnInfo(name, type));
                    }
                }
                long rowCount = 0;
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        rowCount = rs.getLong(1);
                    }
                }
                List<List<Object>> sampleRows = new ArrayList<>();
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 3")) {
                    ResultSetMetaData md = rs.getMetaData();
                    int colCount = md.getColumnCount();
                    while (rs.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            row.add(rs.getObject(i));
                        }
                        sampleRows.add(row);
                    }
                }
                schemas.add(new TableSchema(table, columns, rowCount, sampleRows));
            }
        } catch (Exception e) {
            throw new BizException("读取数据库结构失败: " + e.getMessage());
        }
        return schemas;
    }

    /**
     * 练习库中的表关系（静态定义，与建表列命名约定一致）。
     */
    public List<TableRelation> getRelations() {
        return List.of(
                new TableRelation("products", "category_id", "categories", "category_id"),
                new TableRelation("orders", "customer_id", "customers", "customer_id"),
                new TableRelation("order_items", "order_id", "orders", "order_id"),
                new TableRelation("order_items", "product_id", "products", "product_id"),
                new TableRelation("employees", "department_id", "departments", "department_id"),
                new TableRelation("employees", "manager_id", "employees", "employee_id")
        );
    }

    private String friendlyType(String type) {
        if (type == null) {
            return "";
        }
        return switch (type.toUpperCase()) {
            case "INTEGER", "INT", "BIGINT" -> "INT";
            case "CHARACTER VARYING", "VARCHAR", "CHAR", "CHARACTER" -> "VARCHAR";
            case "NUMERIC", "DECIMAL" -> "DECIMAL";
            case "DATE" -> "DATE";
            default -> type;
        };
    }

    public SqlResult executeSql(Long userId, String sql) {
        try (Connection conn = getDataSource(userId).getConnection();
             Statement st = conn.createStatement()) {
            st.setQueryTimeout(10);
            boolean hasResultSet = st.execute(sql);
            if (hasResultSet) {
                try (ResultSet rs = st.getResultSet()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int colCount = md.getColumnCount();
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        columns.add(md.getColumnLabel(i).toLowerCase());
                    }
                    List<List<Object>> rows = new ArrayList<>();
                    int rowCount = 0;
                    while (rs.next() && rowCount < 200) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            row.add(rs.getObject(i));
                        }
                        rows.add(row);
                        rowCount++;
                    }
                    return new SqlResult(columns, rows, rowCount, null);
                }
            } else {
                int updateCount = st.getUpdateCount();
                return new SqlResult(List.of(), List.of(), updateCount, null);
            }
        } catch (Exception e) {
            return new SqlResult(List.of(), List.of(), 0, e.getMessage());
        }
    }

    private HikariDataSource getDataSource(Long userId) {
        return dataSources.computeIfAbsent(userId, id -> {
            try {
                Path dir = Paths.get(dbDir);
                Files.createDirectories(dir);
                String path = dir.resolve("u_" + id).toAbsolutePath().toString().replace('\\', '/');
                String url = "jdbc:h2:file:" + path + ";MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE";
                HikariConfig cfg = new HikariConfig();
                cfg.setJdbcUrl(url);
                cfg.setUsername("sa");
                cfg.setPassword("");
                cfg.setMaximumPoolSize(2);
                cfg.setPoolName("practice-" + id);
                return new HikariDataSource(cfg);
            } catch (Exception e) {
                throw new BizException("创建专属数据库失败: " + e.getMessage());
            }
        });
    }
}
