package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Transaction;
import model.User;
import service.ATMService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight Java HTTP Server for ATM Simulation System.
 * Built using standard JDK com.sun.net.httpserver API (No external frameworks).
 * Serves static web assets (frontend/) and exposes RESTful API endpoints.
 */
public class ATMHttpServer {

    private static final int PORT = 8080;
    private static final ATMService atmService = new ATMService();
    private static final Map<String, Integer> pinAttemptsMap = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Starts the HTTP Server on port 8080.
     */
    public static void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // API Routes
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/balance", new BalanceHandler());
            server.createContext("/api/withdraw", new WithdrawHandler());
            server.createContext("/api/deposit", new DepositHandler());
            server.createContext("/api/ministatement", new MiniStatementHandler());

            // Static File Handler (HTML, CSS, JS)
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(null); // Default executor
            server.start();

            System.out.println("=================================================");
            System.out.println("🚀 ATM Web Server is running!");
            System.out.println("🌐 Open your browser at: http://localhost:" + PORT);
            System.out.println("=================================================");

        } catch (IOException e) {
            System.err.println("❌ Failed to start HTTP Server: " + e.getMessage());
        }
    }

    // ==========================================
    // API HANDLERS
    // ==========================================

    /**
     * POST /api/login
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (addCorsAndCheckOptions(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method Not Allowed\"}");
                return;
            }

            String requestBody = readRequestBody(exchange);
            Map<String, String> bodyMap = parseJson(requestBody);
            String accountNumber = bodyMap.get("accountNumber");
            String pin = bodyMap.get("pin");

            if (accountNumber == null || accountNumber.isEmpty() || pin == null || pin.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Account number and PIN are required.\"}");
                return;
            }

            try {
                User user = atmService.fetchUserForLogin(accountNumber);
                if (user == null) {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Account not found. Please verify your account number.\"}");
                    return;
                }

                if (user.isLocked()) {
                    sendJsonResponse(exchange, 403, "{\"success\":false,\"message\":\"🔒 Account is LOCKED due to previous failed attempts. Please contact customer support.\"}");
                    return;
                }

                if (user.getPin().equals(pin)) {
                    pinAttemptsMap.remove(accountNumber); // Reset attempts on success
                    String responseJson = String.format(
                            "{\"success\":true,\"message\":\"Login successful\",\"accountNumber\":\"%s\",\"name\":\"%s\",\"balance\":%.2f}",
                            escapeJson(user.getAccountNumber()),
                            escapeJson(user.getName()),
                            user.getBalance()
                    );
                    sendJsonResponse(exchange, 200, responseJson);
                } else {
                    int attempts = pinAttemptsMap.getOrDefault(accountNumber, 0) + 1;
                    pinAttemptsMap.put(accountNumber, attempts);
                    int remaining = ATMService.MAX_PIN_ATTEMPTS - attempts;

                    if (remaining > 0) {
                        String msg = String.format("Incorrect PIN. Attempts remaining: %d", remaining);
                        sendJsonResponse(exchange, 401, String.format("{\"success\":false,\"message\":\"%s\"}", escapeJson(msg)));
                    } else {
                        atmService.lockAccount(accountNumber);
                        pinAttemptsMap.remove(accountNumber);
                        sendJsonResponse(exchange, 403, "{\"success\":false,\"message\":\"🔒 Maximum 3 failed PIN attempts reached. Your account has been LOCKED.\"}");
                    }
                }
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, String.format("{\"success\":false,\"message\":\"Database error: %s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    /**
     * GET /api/balance?accountNumber=1001
     */
    static class BalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (addCorsAndCheckOptions(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method Not Allowed\"}");
                return;
            }

            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String accountNumber = queryParams.get("accountNumber");

            if (accountNumber == null || accountNumber.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Account number is required.\"}");
                return;
            }

            try {
                double balance = atmService.checkBalance(accountNumber);
                String responseJson = String.format("{\"success\":true,\"accountNumber\":\"%s\",\"balance\":%.2f}", escapeJson(accountNumber), balance);
                sendJsonResponse(exchange, 200, responseJson);
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, String.format("{\"success\":false,\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    /**
     * POST /api/withdraw
     */
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (addCorsAndCheckOptions(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method Not Allowed\"}");
                return;
            }

            String requestBody = readRequestBody(exchange);
            Map<String, String> bodyMap = parseJson(requestBody);
            String accountNumber = bodyMap.get("accountNumber");
            String amountStr = bodyMap.get("amount");

            if (accountNumber == null || amountStr == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Account number and amount are required.\"}");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid amount. Please enter a valid number.\"}");
                return;
            }

            try {
                User user = atmService.fetchUserForLogin(accountNumber);
                if (user == null) {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Account not found.\"}");
                    return;
                }

                if (user.isLocked()) {
                    sendJsonResponse(exchange, 403, "{\"success\":false,\"message\":\"Account is locked.\"}");
                    return;
                }

                boolean success = atmService.withdraw(user, amount);
                if (success) {
                    double remainingBalance = atmService.checkBalance(accountNumber);
                    String responseJson = String.format(
                            "{\"success\":true,\"message\":\"Withdrawal successful!\",\"withdrawnAmount\":%.2f,\"remainingBalance\":%.2f}",
                            amount, remainingBalance
                    );
                    sendJsonResponse(exchange, 200, responseJson);
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Withdrawal failed. Check balance or amount limit.\"}");
                }

            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, String.format("{\"success\":false,\"message\":\"Transaction failed: %s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    /**
     * POST /api/deposit
     */
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (addCorsAndCheckOptions(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method Not Allowed\"}");
                return;
            }

            String requestBody = readRequestBody(exchange);
            Map<String, String> bodyMap = parseJson(requestBody);
            String accountNumber = bodyMap.get("accountNumber");
            String amountStr = bodyMap.get("amount");

            if (accountNumber == null || amountStr == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Account number and amount are required.\"}");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid amount. Please enter a valid number.\"}");
                return;
            }

            try {
                User user = atmService.fetchUserForLogin(accountNumber);
                if (user == null) {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Account not found.\"}");
                    return;
                }

                boolean success = atmService.deposit(user, amount);
                if (success) {
                    double updatedBalance = atmService.checkBalance(accountNumber);
                    String responseJson = String.format(
                            "{\"success\":true,\"message\":\"Deposit successful!\",\"depositedAmount\":%.2f,\"updatedBalance\":%.2f}",
                            amount, updatedBalance
                    );
                    sendJsonResponse(exchange, 200, responseJson);
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Deposit failed. Amount must be greater than zero.\"}");
                }

            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, String.format("{\"success\":false,\"message\":\"Transaction failed: %s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    /**
     * GET /api/ministatement?accountNumber=1001
     */
    static class MiniStatementHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (addCorsAndCheckOptions(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method Not Allowed\"}");
                return;
            }

            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String accountNumber = queryParams.get("accountNumber");

            if (accountNumber == null || accountNumber.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Account number is required.\"}");
                return;
            }

            try {
                List<Transaction> transactions = atmService.getMiniStatement(accountNumber, 5);
                double currentBalance = atmService.checkBalance(accountNumber);

                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{\"success\":true,\"currentBalance\":").append(String.format("%.2f", currentBalance));
                jsonBuilder.append(",\"transactions\":[");

                for (int i = 0; i < transactions.size(); i++) {
                    Transaction tx = transactions.get(i);
                    String dateStr = tx.getTransactionDate() != null ? dateFormat.format(tx.getTransactionDate()) : "N/A";
                    jsonBuilder.append(String.format(
                            "{\"date\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"balanceAfter\":%.2f}",
                            escapeJson(dateStr),
                            escapeJson(tx.getTransactionType()),
                            tx.getAmount(),
                            tx.getBalanceAfterTransaction()
                    ));
                    if (i < transactions.size() - 1) {
                        jsonBuilder.append(",");
                    }
                }

                jsonBuilder.append("]}");
                sendJsonResponse(exchange, 200, jsonBuilder.toString());

            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, String.format("{\"success\":false,\"message\":\"%s\"}", escapeJson(e.getMessage())));
            }
        }
    }

    // ==========================================
    // STATIC FILE HANDLER (frontend/)
    // ==========================================

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File("frontend" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("src/../frontend" + path);
            }

            if (!file.exists() || file.isDirectory()) {
                String response = "404 File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            String mimeType = getMimeType(path);
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, file.length());

            OutputStream os = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        }

        private String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }
    }

    // ==========================================
    // HELPER UTILITIES
    // ==========================================

    private static boolean addCorsAndCheckOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String clean = json.trim().replaceAll("^\\{|\\}$", "");
        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, value);
            }
        }
        return map;
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    map.put(key, value);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
