package cafepossystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SalesRecordWindow extends JFrame {
    private JFrame parentWindow;
    private JComboBox<String> dateFilter;
    private JComboBox<String> productFilter;
    private JTable summaryTable;
    private JTable recordsTable;
    private JLabel totalSalesLabel;
    private DefaultTableModel summaryTableModel;
    private DefaultTableModel recordsTableModel;
    private List<SalesItem> processedData;
    
    // Color constants
    private static final Color BACKGROUND_COLOR = new Color(218, 204, 186);
    private static final Color BUTTON_COLOR = new Color(137, 91, 74);
    private static final Color BUTTON_HOVER_COLOR = new Color(107, 68, 55);
    private static final Color ACCENT_COLOR = new Color(255, 165, 0);
    private static final Color HEADER_COLOR = new Color(59, 38, 33);
    private static final Color TEXT_COLOR = new Color(59, 38, 33);
    
    // CSV file path
    private static final String CSV_FILE_PATH = "order-summary.csv";
    
    public SalesRecordWindow(JFrame parent) {
        this.parentWindow = parent;
        setupUI();
        setupStyles();
        loadSalesData();
    }
    
    // NEW METHOD: Add a new sale record to CSV
    public static void addSaleRecord(String customerName, List<String> orderItems, 
                                   double subtotal, double total, double change) {
        try {
            // Create file if it doesn't exist
            File csvFile = new File(CSV_FILE_PATH);
            boolean fileExists = csvFile.exists();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile, true))) {
                // Add header if file is new
                if (!fileExists) {
                    writer.println("timestamp,orders,subtotal,total,change,customer");
                }
                
                // Format current timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                
                // Format order items as string
                String ordersStr = orderItems.toString().replace(",", "',");
                
                // Write the new record
                writer.printf("%s,\"%s\",%.2f,%.2f,%.2f,%s%n",
                    timestamp, ordersStr, subtotal, total, change, 
                    customerName != null ? customerName : "Walk-in");
            }
            
            System.out.println("Sale record added successfully to " + CSV_FILE_PATH);
            
        } catch (IOException e) {
            System.err.println("Error adding sale record: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // NEW METHOD: Add sale record with order details
    public static void addSaleRecord(String customerName, Map<String, Integer> orderDetails, 
                                   double subtotal, double total, double change) {
        List<String> orderItems = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : orderDetails.entrySet()) {
            String item = entry.getKey();
            int quantity = entry.getValue();
            
            if (quantity > 1) {
                orderItems.add(item + " " + quantity + "x");
            } else {
                orderItems.add(item + " 1x");
            }
        }
        
        addSaleRecord(customerName, orderItems, subtotal, total, change);
    }
    
    // NEW METHOD: Refresh data (call this after adding new records)
    public void refreshData() {
        loadSalesData();
        JOptionPane.showMessageDialog(this, "Sales data refreshed successfully!", 
            "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void setupUI() {
        setTitle("Sales Records - Cafe POS");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Filter panel
        JPanel filterPanel = createFilterPanel();
        
        // Content panel with split pane
        JSplitPane splitPane = createContentPanel();
        
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(filterPanel, BorderLayout.CENTER);
        mainPanel.add(splitPane, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("SALES RECORDS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        
        JButton backButton = new JButton("← Back to POS");
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> dispose());
        styleButton(backButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Date filter
        filterPanel.add(new JLabel("Filter by Date:"));
        dateFilter = new JComboBox<>(new String[]{
            "All Time", "Today", "Yesterday", "Last 7 Days",
            "Last 30 Days", "This Month", "Custom Range"
        });
        dateFilter.addActionListener(e -> filterChanged());
        filterPanel.add(dateFilter);
        
        // Product filter
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Filter by Product:"));
        productFilter = new JComboBox<>();
        productFilter.addItem("All Products");
        productFilter.addActionListener(e -> filterChanged());
        filterPanel.add(productFilter);
        
        // Refresh button
        filterPanel.add(Box.createHorizontalStrut(20));
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshData()); // Updated to use new method
        styleButton(refreshButton);
        filterPanel.add(refreshButton);
        
       
        
        return filterPanel;
    }
    
    // NEW METHOD: Add a test sale for demonstration
    private void addTestSale() {
        List<String> testOrder = Arrays.asList("Hot Americano Reg 1x", "Cold Latte Lrg 1x");
        double subtotal = 215.0;
        double total = 215.0;
        double change = 35.0;
        
        addSaleRecord("Test Customer", testOrder, subtotal, total, change);
        refreshData();
    }
    
    private JSplitPane createContentPanel() {
        // Summary panel
        JPanel summaryPanel = createSummaryPanel();
        
        // Records panel
        JPanel recordsPanel = createRecordsPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, summaryPanel, recordsPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);
        
        return splitPane;
    }
    
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel summaryTitle = new JLabel("SALES SUMMARY", SwingConstants.CENTER);
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 16));
        summaryTitle.setForeground(TEXT_COLOR);
        
        // Summary table
        String[] summaryColumns = {"Product", "Qty Sold", "Total Sales"};
        summaryTableModel = new DefaultTableModel(summaryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        summaryTable = new JTable(summaryTableModel);
        styleTable(summaryTable);
        
        JScrollPane summaryScrollPane = new JScrollPane(summaryTable);
        summaryScrollPane.setPreferredSize(new Dimension(350, 300));
        
        // Total sales label
        totalSalesLabel = new JLabel("TOTAL SALES: ₱0.00", SwingConstants.CENTER);
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalSalesLabel.setForeground(Color.WHITE);
        totalSalesLabel.setOpaque(true);
        totalSalesLabel.setBackground(HEADER_COLOR);
        totalSalesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        summaryPanel.add(summaryTitle, BorderLayout.NORTH);
        summaryPanel.add(summaryScrollPane, BorderLayout.CENTER);
        summaryPanel.add(totalSalesLabel, BorderLayout.SOUTH);
        
        return summaryPanel;
    }
    
    private JPanel createRecordsPanel() {
        JPanel recordsPanel = new JPanel(new BorderLayout());
        recordsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel recordsTitle = new JLabel("DETAILED RECORDS", SwingConstants.CENTER);
        recordsTitle.setFont(new Font("Arial", Font.BOLD, 16));
        recordsTitle.setForeground(TEXT_COLOR);
        
        // Records table
        String[] recordsColumns = {"Date/Time", "Customer", "Product", "Quantity", "Unit Price", "Total"};
        recordsTableModel = new DefaultTableModel(recordsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recordsTable = new JTable(recordsTableModel);
        styleTable(recordsTable);
        
        // Set column widths
        TableColumnModel columnModel = recordsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(100);
        columnModel.getColumn(5).setPreferredWidth(100);
        
        JScrollPane recordsScrollPane = new JScrollPane(recordsTable);
        recordsScrollPane.setPreferredSize(new Dimension(550, 300));
        
        // Export button
        JButton exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(e -> exportToCSV());
        styleButton(exportButton);
        
        recordsPanel.add(recordsTitle, BorderLayout.NORTH);
        recordsPanel.add(recordsScrollPane, BorderLayout.CENTER);
        recordsPanel.add(exportButton, BorderLayout.SOUTH);
        
        return recordsPanel;
    }
    
    private void styleButton(JButton button) {
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(25);
        table.setGridColor(new Color(221, 221, 221));
        table.setSelectionBackground(ACCENT_COLOR);
        table.setSelectionForeground(Color.WHITE);
        
        // Style header
        table.getTableHeader().setBackground(HEADER_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setOpaque(true);
    }
    
    private void setupStyles() {
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadSalesData() {
    try {
        File csvFile = new File(CSV_FILE_PATH);
        if (!csvFile.exists()) {
            // Create empty CSV file with header instead of sample data
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
                writer.println("timestamp,orders,subtotal,total,change,customer");
            }
            // Initialize empty data structures
            processedData = new ArrayList<>();
            updateProductFilter();
            createEmptyTables();
            return;
        }
        
        processedData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine && line.startsWith("timestamp")) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] parts = line.split(",", 6);
                if (parts.length >= 6) {
                    processOrderData(parts);
                }
                isFirstLine = false;
            }
        }
        
        updateProductFilter();
        filterChanged();
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading sales data: " + e.getMessage(),
            "Error", JOptionPane.WARNING_MESSAGE);
        createEmptyTables();
    }
}
    
    
    
    private void processOrderData(String[] csvParts) {
        try {
            LocalDateTime timestamp = LocalDateTime.parse(csvParts[0]);
            String ordersStr = csvParts[1].replaceAll("[\\[\\]']", "");
            String customer = csvParts.length > 5 ? csvParts[5] : "Walk-in";
            
            String[] orders = ordersStr.split("', '");
            for (String order : orders) {
                order = order.trim();
                if (!order.isEmpty()) {
                    SalesItem item = parseOrderItem(order.replace("'", ""));
                    if (item != null) {
                        item.timestamp = timestamp;
                        item.customer = customer;
                        processedData.add(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing order: " + e.getMessage());
        }
    }
    
    private SalesItem parseOrderItem(String orderStr) {
        try {
            int quantity = 1;
            String itemName = orderStr;
            
            // Extract quantity
            if (orderStr.contains("x")) {
                String[] parts = orderStr.split("x");
                if (parts.length > 1) {
                    String lastPart = parts[parts.length - 1];
                    if (lastPart.matches("\\d+")) {
                        quantity = Integer.parseInt(lastPart);
                        itemName = orderStr.substring(0, orderStr.lastIndexOf(lastPart + "x"));
                    }
                }
            }
            
            double basePrice = getBasePrice(itemName);
            double unitPrice = calculateUnitPrice(itemName, basePrice);
            
            SalesItem item = new SalesItem();
            item.product = itemName.trim();
            item.quantity = quantity;
            item.unitPrice = unitPrice;
            item.totalPrice = unitPrice * quantity;
            
            return item;
        } catch (Exception e) {
            System.err.println("Error parsing order item: " + e.getMessage());
            return null;
        }
    }
    
    private double getBasePrice(String itemName) {
        Map<String, Double> priceMap = new HashMap<>();
        priceMap.put("Americano", 100.0);
        priceMap.put("Cappuccino", 110.0);
        priceMap.put("CafeLatte", 110.0);
        priceMap.put("SpLatte", 120.0);
        priceMap.put("Matcha", 120.0);
        priceMap.put("Chocolate", 120.0);
        
        for (Map.Entry<String, Double> entry : priceMap.entrySet()) {
            if (itemName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 100.0; // Default price
    }
    
    private double calculateUnitPrice(String itemName, double basePrice) {
        double price = basePrice;
        if (itemName.contains("Lrg")) {
            price += 10;
        }
        if (itemName.contains("Cold")) {
            price += 5;
        }
        return price;
    }
    
    private void updateProductFilter() {
        productFilter.removeAllItems();
        productFilter.addItem("All Products");
        
        if (processedData != null) {
            Set<String> products = processedData.stream()
                .map(item -> item.product)
                .collect(Collectors.toSet());
            
            products.stream().sorted().forEach(productFilter::addItem);
        }
    }
    
    private void filterChanged() {
        if (processedData == null) return;
        
        List<SalesItem> filteredData = new ArrayList<>(processedData);
        
        // Apply date filter
        String dateFilterValue = (String) dateFilter.getSelectedItem();
        if (!"All Time".equals(dateFilterValue)) {
            filteredData = applyDateFilter(filteredData, dateFilterValue);
        }
        
        // Apply product filter
        String productFilterValue = (String) productFilter.getSelectedItem();
        if (!"All Products".equals(productFilterValue)) {
            filteredData = filteredData.stream()
                .filter(item -> item.product.equals(productFilterValue))
                .collect(Collectors.toList());
        }
        
        updateSummaryTable(filteredData);
        updateRecordsTable(filteredData);
    }
    
    private List<SalesItem> applyDateFilter(List<SalesItem> data, String dateFilter) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (dateFilter) {
            case "Today":
                startDate = now.toLocalDate().atStartOfDay();
                break;
            case "Yesterday":
                LocalDateTime yesterday = now.minusDays(1);
                startDate = yesterday.toLocalDate().atStartOfDay();
                LocalDateTime endDate = now.toLocalDate().atStartOfDay();
                return data.stream()
                    .filter(item -> item.timestamp.isAfter(startDate) && item.timestamp.isBefore(endDate))
                    .collect(Collectors.toList());
            case "Last 7 Days":
                startDate = now.minusDays(7);
                break;
            case "Last 30 Days":
                startDate = now.minusDays(30);
                break;
            case "This Month":
                startDate = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                break;
            default:
                return data;
        }
        
        return data.stream()
            .filter(item -> item.timestamp.isAfter(startDate))
            .collect(Collectors.toList());
    }
    
    private void updateSummaryTable(List<SalesItem> data) {
        // Clear existing data
        summaryTableModel.setRowCount(0);
        
        // Aggregate data by product
        Map<String, ProductSummary> summary = new HashMap<>();
        
        for (SalesItem item : data) {
            ProductSummary productSummary = summary.computeIfAbsent(item.product, 
                k -> new ProductSummary());
            productSummary.quantity += item.quantity;
            productSummary.total += item.totalPrice;
        }
        
        // Add rows to table
        double totalSales = 0;
        for (Map.Entry<String, ProductSummary> entry : summary.entrySet()) {
            String product = entry.getKey();
            ProductSummary stats = entry.getValue();
            
            summaryTableModel.addRow(new Object[]{
                product,
                stats.quantity,
                String.format("₱%.2f", stats.total)
            });
            
            totalSales += stats.total;
        }
        
        // Update total sales label
        totalSalesLabel.setText(String.format("TOTAL SALES: ₱%.2f", totalSales));
    }
    
    private void updateRecordsTable(List<SalesItem> data) {
        // Clear existing data
        recordsTableModel.setRowCount(0);
        
        // Sort by timestamp (newest first)
        data.stream()
            .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
            .forEach(item -> {
                recordsTableModel.addRow(new Object[]{
                    item.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    item.customer,
                    item.product,
                    item.quantity,
                    String.format("₱%.2f", item.unitPrice),
                    String.format("₱%.2f", item.totalPrice)
                });
            });
    }
    
    private void createEmptyTables() {
        summaryTableModel.setRowCount(0);
        recordsTableModel.setRowCount(0);
        totalSalesLabel.setText("TOTAL SALES: ₱0.00");
    }
    
    private void exportToCSV() {
        try {
            List<SalesItem> filteredData = getCurrentFilteredData();
            
            if (filteredData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No data matches the current filters.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Generate filename with timestamp
            String filename = "sales_report_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                // Write header
                writer.println("Date/Time,Customer,Product,Quantity,Unit Price,Total Price");
                
                // Write data rows
                for (SalesItem item : filteredData) {
                    writer.printf("%s,%s,%s,%d,%.2f,%.2f%n",
                        item.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        item.customer,
                        item.product,
                        item.quantity,
                        item.unitPrice,
                        item.totalPrice);
                }
            }
            
            JOptionPane.showMessageDialog(this, "Sales report exported to " + filename,
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private List<SalesItem> getCurrentFilteredData() {
        if (processedData == null) return new ArrayList<>();
        
        List<SalesItem> filteredData = new ArrayList<>(processedData);
        
        // Apply current filters
        String dateFilterValue = (String) dateFilter.getSelectedItem();
        if (!"All Time".equals(dateFilterValue)) {
            filteredData = applyDateFilter(filteredData, dateFilterValue);
        }
        
        String productFilterValue = (String) productFilter.getSelectedItem();
        if (!"All Products".equals(productFilterValue)) {
            filteredData = filteredData.stream()
                .filter(item -> item.product.equals(productFilterValue))
                .collect(Collectors.toList());
        }
        
        return filteredData;
    }
    
    // Helper classes
    private static class SalesItem {
        LocalDateTime timestamp;
        String customer;
        String product;
        int quantity;
        double unitPrice;
        double totalPrice;
    }
    
    private static class ProductSummary {
        int quantity = 0;
        double total = 0.0;
    }
    
    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            SalesRecordWindow window = new SalesRecordWindow(null);
            window.setVisible(true);
        });
    }
}