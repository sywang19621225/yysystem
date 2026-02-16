package com.yysystem.modules.system.controller;

import com.yysystem.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/diagnostics")
public class SystemDiagnosticsController {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-info")
    public Result<Map<String, Object>> dbInfo() {
        Map<String, Object> info = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String url = meta.getURL();
            String user = meta.getUserName();
            String product = meta.getDatabaseProductName();
            String version = meta.getDatabaseProductVersion();
            String catalog = conn.getCatalog();

            info.put("jdbcUrl", url);
            info.put("username", user);
            info.put("product", product);
            info.put("version", version);
            info.put("database", catalog);

            String host = "";
            String port = "";
            try {
                // jdbc:mysql://localhost:3306/yysystem...
                String noPrefix = url.replace("jdbc:mysql://", "");
                String[] parts = noPrefix.split("/");
                String hostPort = parts[0];
                String[] hp = hostPort.split(":");
                host = hp[0];
                port = hp.length > 1 ? hp[1] : "3306";
            } catch (Exception ignore) {}
            info.put("host", host);
            info.put("port", port);
        } catch (Exception e) {
            return Result.error("Failed to read DB info: " + e.getMessage());
        }
        return Result.success(info);
    }

    @GetMapping("/table-columns")
    public Result<List<Map<String, Object>>> tableColumns(@RequestParam String table) {
        try (Connection conn = dataSource.getConnection()) {
            String schema = conn.getCatalog();
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT " +
                    "FROM information_schema.columns WHERE table_schema=? AND table_name=? ORDER BY ORDINAL_POSITION";
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(sql, schema, table);
            return Result.success(cols);
        } catch (Exception e) {
            return Result.error("Failed to read columns: " + e.getMessage());
        }
    }

    @GetMapping("/check-tech-info-table")
    public Result<Map<String, Object>> checkTechInfoTable() {
        Map<String, Object> res = new HashMap<>();
        String table = "crm_customer_tech_info";
        String[] required = new String[]{
                "system_name", "ip_address", "remote_method", "remote_account", "remote_password",
                "software_url", "remark",
                "location", "system_account", "system_password",
                "tech_leader", "tech_contact", "tech_wechat"
        };
        try (Connection conn = dataSource.getConnection()) {
            String schema = conn.getCatalog();
            String sql = "SELECT COLUMN_NAME FROM information_schema.columns WHERE table_schema=? AND table_name=?";
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(sql, schema, table);
            res.put("table", table);
            res.put("schema", schema);
            res.put("columns", cols);
            Map<String, Boolean> status = new HashMap<>();
            for (String c : required) {
                boolean present = cols.stream().anyMatch(m -> c.equalsIgnoreCase(String.valueOf(m.get("COLUMN_NAME"))));
                status.put(c, present);
            }
            res.put("requiredStatus", status);
        } catch (Exception e) {
            return Result.error("Failed to check table: " + e.getMessage());
        }
        return Result.success(res);
    }
}
