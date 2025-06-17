package cafepossystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Color;
// Order Item Class
class OrderItem extends JPanel {
    private String orderName;
    private double orderPrice;
    private int orderQty;
    private double orderTotal;
    private OrderItemDeleteListener deleteListener;
    
    public interface OrderItemDeleteListener {
        void onDeleteClicked(OrderItem item);
    }
    
    public OrderItem(String orderName, double orderPrice, int orderQty, double orderTotal) {
        this.orderName = orderName;
        this.orderPrice = orderPrice;
        this.orderQty = orderQty;
        this.orderTotal = orderTotal;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setPreferredSize(new Dimension(340, 40));
        
        JLabel nameLabel = new JLabel(orderName);
        nameLabel.setPreferredSize(new Dimension(120, 25));
        
        JLabel qtyLabel = new JLabel(String.valueOf(orderQty));
        qtyLabel.setPreferredSize(new Dimension(30, 25));
        
        JLabel priceLabel = new JLabel("₱" + String.format("%.2f", orderPrice));
        priceLabel.setPreferredSize(new Dimension(60, 25));
        
        JLabel totalLabel = new JLabel("₱" + String.format("%.2f", orderTotal));
        totalLabel.setPreferredSize(new Dimension(70, 25));
        
        JButton deleteBtn = new JButton("×");
        deleteBtn.setPreferredSize(new Dimension(25, 25));
        deleteBtn.setBackground(new Color(220, 53, 69));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBorder(BorderFactory.createEmptyBorder());
        deleteBtn.addActionListener(e -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClicked(this);
            }
        });
        
        add(nameLabel);
        add(qtyLabel);
        add(priceLabel);
        add(totalLabel);
        add(deleteBtn);
    }
    
    public void setDeleteListener(OrderItemDeleteListener listener) {
        this.deleteListener = listener;
    }
    
    // Getters
    public String getOrderName() { return orderName; }
    public double getOrderPrice() { return orderPrice; }
    public int getOrderQty() { return orderQty; }
    public double getOrderTotal() { return orderTotal; }
}

// Previous Order Item Class
class PreviousOrderItem extends JPanel {
    private String customerName;
    private List<String> orderItems;
    private boolean completed;
    private JCheckBox checkbox;
    private JLabel customerLabel;
    private JLabel itemsLabel;
    
    public PreviousOrderItem(String customerName, List<String> orderItems) {
        this.customerName = customerName;
        this.orderItems = orderItems;
        this.completed = false;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setPreferredSize(new Dimension(240, 80));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkbox = new JCheckBox();
        checkbox.addActionListener(e -> toggleCompletion());
        
        customerLabel = new JLabel(customerName.toUpperCase());
        customerLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        topPanel.add(checkbox);
        topPanel.add(customerLabel);
        
        itemsLabel = new JLabel("<html>" + String.join("<br>", orderItems) + "</html>");
        itemsLabel.setVerticalAlignment(SwingConstants.TOP);
        
        add(topPanel, BorderLayout.NORTH);
        add(itemsLabel, BorderLayout.CENTER);
    }
    
    private void toggleCompletion() {
        completed = checkbox.isSelected();
        if (completed) {
            setBackground(new Color(240, 240, 240));
            customerLabel.setForeground(Color.GRAY);
            itemsLabel.setForeground(Color.GRAY);
        } else {
            setBackground(Color.WHITE);
            customerLabel.setForeground(Color.BLACK);
            itemsLabel.setForeground(Color.BLACK);
        }
    }
}

// Receipt Dialog Class
class ReceiptDialog extends JDialog {
    private String customerName;
    private List<OrderItem> orderItems;
    private double total;
    private double received;
    private double change;
    private JTextArea receiptText;
    private Runnable backClickCallback;
    
    public ReceiptDialog(JFrame parent, String customerName, List<OrderItem> orderItems, 
                        double total, double received, double change) {
        super(parent, "Receipt", true);
        this.customerName = customerName;
        this.orderItems = orderItems;
        this.total = total;
        this.received = received;
        this.change = change;
        setupUI();
    }
    
    private void setupUI() {
        setSize(400, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        receiptText = new JTextArea();
        receiptText.setEditable(false);
        receiptText.setFont(new Font("Courier New", Font.PLAIN, 12));
        receiptText.setText(generateReceiptContent());
        
        JScrollPane scrollPane = new JScrollPane(receiptText);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton printBtn = new JButton("Print Receipt");
        JButton backBtn = new JButton("OK");
        
        printBtn.addActionListener(e -> printReceipt());
        backBtn.addActionListener(e -> {
            if (backClickCallback != null) {
                backClickCallback.run();
            }
            dispose();
        });
        
        buttonPanel.add(printBtn);
        buttonPanel.add(backBtn);
        
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void setBackClickCallback(Runnable callback) {
        this.backClickCallback = callback;
    }
    
    private String generateReceiptContent() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("========================================\n");
        receipt.append("         CAFE RECEIPT\n");
        receipt.append("========================================\n");
        receipt.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        receipt.append("Customer: ").append(customerName != null && !customerName.isEmpty() ? customerName : "Walk-in").append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append("ITEMS:\n");
        receipt.append("----------------------------------------\n");
        
        for (OrderItem item : orderItems) {
            receipt.append(item.getOrderName()).append("\n");
            receipt.append("  ").append(item.getOrderQty()).append(" x ₱")
                   .append(String.format("%.2f", item.getOrderPrice())).append(" = ₱")
                   .append(String.format("%.2f", item.getOrderTotal())).append("\n\n");
        }
        
        receipt.append("----------------------------------------\n");
        receipt.append("TOTAL:           ₱").append(String.format("%.2f", total)).append("\n");
        receipt.append("RECEIVED:        ₱").append(String.format("%.2f", received)).append("\n");
        receipt.append("CHANGE:          ₱").append(String.format("%.2f", change)).append("\n");
        receipt.append("========================================\n");
        receipt.append("    Thank you for your visit!\n");
        receipt.append("========================================\n");
        
        return receipt.toString();
    }
    
    private void printReceipt() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            if (job.printDialog()) {
                receiptText.print();
                JOptionPane.showMessageDialog(this, "Receipt printed successfully!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error printing receipt: " + e.getMessage());
        }
    }
}

// Sales Record Window Class


// Main Cafe POS Application
public class CafePOSApp extends JFrame implements OrderItem.OrderItemDeleteListener {
    // Current order variables
    private String currentDrinkName = "";
    private double currentBasePrice = 0;
    private String currentSize = "";
    private String currentTemp = "";
    private int currentQuantity = 1;
    
    // Order management
    private List<OrderItem> orderItems = new ArrayList<>();
    private List<Double> payAmounts = new ArrayList<>();
    private List<PreviousOrderItem> previousOrders = new ArrayList<>();
    
    // UI Components
    private JTextField customerInput;
    private JPanel orderListPanel;
    private JScrollPane orderScrollPane;
    private JLabel totalLabel;
    private JLabel receivedLabel;
    private JLabel changeLabel;
    private JTextField amountInput;
    private JTextField qtyInput;
    private JPanel previousListPanel;
    
    // Drink buttons
    private JButton[] drinkButtons = new JButton[6];
    private JButton regBtn, lrgBtn, hotBtn, coldBtn, addBtn;
    
    // Colors
    private final Color MAIN_BG = new Color(218, 204, 186);
    private final Color BUTTON_BG = new Color(137, 91, 74);
    private final Color BUTTON_PRESSED = new Color(255, 165, 0);
    private final Color DARK_BROWN = new Color(59, 38, 33);
    private final Color RED_BG = new Color(220, 53, 69);
    private final Color GREEN_BG = new Color(40, 167, 69);
    
    public CafePOSApp() {
        initVariables();
        setupUI();
        setupStyles();
    }
    
    private void initVariables() {
        currentQuantity = 1;
    }
    
    private void setupUI() {
        setTitle("Cafe POS System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));
        
        // Left section - Menu
        JPanel menuSection = createMenuSection();
        mainPanel.add(menuSection);
        
        // Center section - Order
        JPanel orderSection = createOrderSection();
        mainPanel.add(orderSection);
        
        // Right section - Previous Orders
        JPanel previousSection = createPreviousOrdersSection();
        mainPanel.add(previousSection);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupStyles() {
        getContentPane().setBackground(MAIN_BG);
        
        // Style buttons
        if (addBtn != null) styleButton(addBtn, DARK_BROWN, Color.WHITE);
        
        // Style drink buttons
        for (JButton btn : drinkButtons) {
            if (btn != null) styleButton(btn, BUTTON_BG, Color.WHITE);
        }
    }
    
    private void styleButton(JButton button, Color bgColor, Color textColor) {
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    private JPanel createMenuSection() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));
        
        // Drink buttons (first row)
        JPanel drinksRow1 = new JPanel(new GridLayout(1, 3));
        drinkButtons[0] = createDrinkButton("Americano\n₱100", 100);
        drinkButtons[1] = createDrinkButton("Cappuccino\n₱110", 110);
        drinkButtons[2] = createDrinkButton("CafeLatte\n₱110", 110);
        
        drinksRow1.add(drinkButtons[0]);
        drinksRow1.add(drinkButtons[1]);
        drinksRow1.add(drinkButtons[2]);
        
        // Drink buttons (second row)
        JPanel drinksRow2 = new JPanel(new GridLayout(1, 3));
        drinkButtons[3] = createDrinkButton("SpLatte\n₱120", 120);
        drinkButtons[4] = createDrinkButton("Matcha\n₱120", 120);
        drinkButtons[5] = createDrinkButton("Chocolate\n₱120", 120);
        
        drinksRow2.add(drinkButtons[3]);
        drinksRow2.add(drinkButtons[4]);
        drinksRow2.add(drinkButtons[5]);
        
        // Size buttons
        JPanel sizeRow = new JPanel(new GridLayout(1, 2));
        regBtn = new JButton("<html>Reg<br>(+₱0)</html>");
        regBtn.setPreferredSize(new Dimension(125, 100));
        regBtn.setEnabled(false);
        regBtn.addActionListener(e -> regClicked());
        
        lrgBtn = new JButton("<html>Lrg<br>(+₱10)</html>");
        lrgBtn.setPreferredSize(new Dimension(125, 100));
        lrgBtn.setEnabled(false);
        lrgBtn.addActionListener(e -> lrgClicked());
        
        sizeRow.add(regBtn);
        sizeRow.add(lrgBtn);
        
        // Temperature buttons
        JPanel tempRow = new JPanel(new GridLayout(1, 2));
        hotBtn = new JButton("<html>Hot<br>(+₱0)</html>");
        hotBtn.setPreferredSize(new Dimension(125, 100));
        hotBtn.setEnabled(false);
        hotBtn.addActionListener(e -> hotClicked());
        
        coldBtn = new JButton("<html>Cold<br>(+₱5)</html>");
        coldBtn.setPreferredSize(new Dimension(125, 100));
        coldBtn.setEnabled(false);
        coldBtn.addActionListener(e -> coldClicked());
        
        tempRow.add(hotBtn);
        tempRow.add(coldBtn);
        
        // Quantity controls
        JPanel qtyRow = new JPanel(new FlowLayout());
        JButton minusBtn = new JButton("-");
        minusBtn.setPreferredSize(new Dimension(60, 40));
        minusBtn.addActionListener(e -> minusClicked());
        
        qtyInput = new JTextField("1");
        qtyInput.setPreferredSize(new Dimension(50, 40));
        qtyInput.setHorizontalAlignment(JTextField.CENTER);
        qtyInput.setEditable(false);
        
        JButton plusBtn = new JButton("+");
        plusBtn.setPreferredSize(new Dimension(60, 40));
        plusBtn.addActionListener(e -> plusClicked());
        
        addBtn = new JButton("ADD");
        addBtn.setPreferredSize(new Dimension(200, 50));
        addBtn.setEnabled(false);
        addBtn.addActionListener(e -> addToOrder());
        
        qtyRow.add(minusBtn);
        qtyRow.add(qtyInput);
        qtyRow.add(plusBtn);
        qtyRow.add(addBtn);
        
        menuPanel.add(drinksRow1);
        menuPanel.add(drinksRow2);
        menuPanel.add(sizeRow);
        menuPanel.add(tempRow);
        menuPanel.add(qtyRow);
        
        return menuPanel;
    }
    
    private JButton createDrinkButton(String nameWithPrice, double price) {
        JButton btn = new JButton("<html><center>" + nameWithPrice.replace("\n", "<br>") + "</center></html>");
        btn.setPreferredSize(new Dimension(125, 200));
        String drinkName = nameWithPrice.split("\n")[0];
        btn.addActionListener(e -> drinkClicked(drinkName, price, btn));
        return btn;
    }
    private JPanel createOrderSection() {
    JPanel orderPanel = new JPanel();
    orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
    orderPanel.setBorder(BorderFactory.createTitledBorder("Order"));
    
    // Header with customer name
    JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel orderTitle = new JLabel("ORDER:");
    orderTitle.setFont(new Font("Arial", Font.BOLD, 24));
    
    customerInput = new JTextField("Customer Name");
    customerInput.setPreferredSize(new Dimension(150, 30));
    customerInput.setForeground(Color.GRAY); // Make placeholder text gray
    
    // Add focus listener for customer name field
    customerInput.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
            if (customerInput.getText().equals("Customer Name")) {
                customerInput.setText("");
                customerInput.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (customerInput.getText().isEmpty()) {
                customerInput.setText("Customer Name");
                customerInput.setForeground(Color.GRAY);
            }
        }
    });
    
    headerPanel.add(orderTitle);
    headerPanel.add(customerInput);
    
    // Order list
    orderListPanel = new JPanel();
    orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
    
    orderScrollPane = new JScrollPane(orderListPanel);
    orderScrollPane.setPreferredSize(new Dimension(350, 200));
    orderScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    // Payment info
    totalLabel = new JLabel("TOTAL: ₱0");
    totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
    
    receivedLabel = new JLabel("RECEIVED: ₱0");
    receivedLabel.setFont(new Font("Arial", Font.BOLD, 16));
    
    changeLabel = new JLabel("CHANGE: ₱0");
    changeLabel.setFont(new Font("Arial", Font.BOLD, 16));
    
    // Payment controls
    JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    amountInput = new JTextField("Amount received");
    amountInput.setPreferredSize(new Dimension(120, 30));
    amountInput.setForeground(Color.GRAY); // Make placeholder text gray
    
    // Add focus listener for amount input field
    amountInput.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
            if (amountInput.getText().equals("Amount received")) {
                amountInput.setText("");
                amountInput.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (amountInput.getText().isEmpty()) {
                amountInput.setText("Amount received");
                amountInput.setForeground(Color.GRAY);
            }
        }
    });
    
    JButton payBtn = new JButton("Pay");
    payBtn.setPreferredSize(new Dimension(100, 40));
    payBtn.addActionListener(e -> processPayment());
    
    paymentPanel.add(amountInput);
    paymentPanel.add(payBtn);
    
    // Button row
    JPanel buttonRow = new JPanel(new FlowLayout());
    JButton resetOrderBtn = new JButton("Reset Order");
    resetOrderBtn.setPreferredSize(new Dimension(125, 50));
    resetOrderBtn.setBackground(RED_BG);
    resetOrderBtn.setForeground(Color.WHITE);
    resetOrderBtn.addActionListener(e -> resetOrder());
    
    JButton salesBtn = new JButton("Sales");
    salesBtn.setPreferredSize(new Dimension(125, 50));
    salesBtn.setBackground(GREEN_BG);
    salesBtn.setForeground(Color.WHITE);
    salesBtn.addActionListener(e -> showSalesRecord());
    
    buttonRow.add(resetOrderBtn);
    buttonRow.add(salesBtn);
    
    orderPanel.add(headerPanel);
    orderPanel.add(orderScrollPane);
    orderPanel.add(totalLabel);
    orderPanel.add(receivedLabel);
    orderPanel.add(changeLabel);
    orderPanel.add(paymentPanel);
    orderPanel.add(buttonRow);
    
    return orderPanel;
}
    
    private JPanel createPreviousOrdersSection() {
        JPanel previousPanel = new JPanel();
        previousPanel.setLayout(new BoxLayout(previousPanel, BoxLayout.Y_AXIS));
        previousPanel.setBorder(BorderFactory.createTitledBorder("Previous Orders"));
        
        JLabel title = new JLabel("Previous Orders");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        
        previousListPanel = new JPanel();
        previousListPanel.setLayout(new BoxLayout(previousListPanel, BoxLayout.Y_AXIS));
        
        JScrollPane previousScrollPane = new JScrollPane(previousListPanel);
        previousScrollPane.setPreferredSize(new Dimension(250, 520));
        previousScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        previousPanel.add(title);
        previousPanel.add(previousScrollPane);
        
        return previousPanel;
    }
    
    // Event handlers
    private void drinkClicked(String drinkName, double price, JButton clickedButton) {
        currentDrinkName = drinkName;
        currentBasePrice = price;
        
        // Reset all drink buttons to default color
        for (JButton btn : drinkButtons) {
            if (btn != null) {
                btn.setBackground(BUTTON_BG);
            }
        }
        
        // Highlight selected button
        clickedButton.setBackground(BUTTON_PRESSED);
        
        // Enable size and temperature buttons
        regBtn.setEnabled(true);
        lrgBtn.setEnabled(true);
        hotBtn.setEnabled(true);
        coldBtn.setEnabled(true);
        
        // Reset selections
        currentSize = "";
        currentTemp = "";
        regBtn.setBackground(BUTTON_BG);
        lrgBtn.setBackground(BUTTON_BG);
        hotBtn.setBackground(BUTTON_BG);
        coldBtn.setBackground(BUTTON_BG);
        
        updateAddButtonState();
    }
    
    private void regClicked() {
        if ("Reg".equals(currentSize)) {
            currentSize = "";
            regBtn.setBackground(BUTTON_BG);
        } else {
            currentSize = "Reg";
            regBtn.setBackground(BUTTON_PRESSED);
            lrgBtn.setBackground(BUTTON_BG);
        }
        updateAddButtonState();
    }
    
    private void lrgClicked() {
        if ("Lrg".equals(currentSize)) {
            currentSize = "";
            lrgBtn.setBackground(BUTTON_BG);
        } else {
            currentSize = "Lrg";
            lrgBtn.setBackground(BUTTON_PRESSED);
            regBtn.setBackground(BUTTON_BG);
        }
        updateAddButtonState();
    }
    
    private void hotClicked() {
        if ("Hot".equals(currentTemp)) {
            currentTemp = "";
            hotBtn.setBackground(BUTTON_BG);
        } else {
            currentTemp = "Hot";
            hotBtn.setBackground(BUTTON_PRESSED);
            coldBtn.setBackground(BUTTON_BG);
        }
        updateAddButtonState();
    }
    
    private void coldClicked() {
        if ("Cold".equals(currentTemp)) {
            currentTemp = "";
            coldBtn.setBackground(BUTTON_BG);
        } else {
            currentTemp = "Cold";
            coldBtn.setBackground(BUTTON_PRESSED);
            hotBtn.setBackground(BUTTON_BG);
        }
        updateAddButtonState();
    }
    
    private void minusClicked() {
        if (currentQuantity > 1) {
            currentQuantity--;
            qtyInput.setText(String.valueOf(currentQuantity));
        }
    }
    
    private void plusClicked() {
        currentQuantity++;
        qtyInput.setText(String.valueOf(currentQuantity));
    }
    
    private void updateAddButtonState() {
        addBtn.setEnabled(!currentDrinkName.isEmpty() && !currentSize.isEmpty() && !currentTemp.isEmpty());
    }
    
    private void addToOrder() {
        double finalPrice = currentBasePrice;
        
        // Add size cost
        if ("Lrg".equals(currentSize)) {
            finalPrice += 10;
        }
        
        // Add temperature cost
        if ("Cold".equals(currentTemp)) {
            finalPrice += 5;
        }
        
        double totalPrice = finalPrice * currentQuantity;
        String itemName = currentDrinkName + " (" + currentSize + ", " + currentTemp + ")";
        
        OrderItem orderItem = new OrderItem(itemName, finalPrice, currentQuantity, totalPrice);
        orderItem.setDeleteListener(this);
        
        orderItems.add(orderItem);
        orderListPanel.add(orderItem);
        orderListPanel.revalidate();
        orderListPanel.repaint();
        
        updateTotals();
        resetCurrentOrder();
    }
    
    private void resetCurrentOrder() {
        currentDrinkName = "";
        currentBasePrice = 0;
        currentSize = "";
        currentTemp = "";
        currentQuantity = 1;
        qtyInput.setText("1");
        
        // Reset button colors
        for (JButton btn : drinkButtons) {
            if (btn != null) {
                btn.setBackground(BUTTON_BG);
            }
        }
        
        // Disable buttons
        regBtn.setEnabled(false);
        lrgBtn.setEnabled(false);
        hotBtn.setEnabled(false);
        coldBtn.setEnabled(false);
        addBtn.setEnabled(false);
        
        regBtn.setBackground(BUTTON_BG);
        lrgBtn.setBackground(BUTTON_BG);
        hotBtn.setBackground(BUTTON_BG);
        coldBtn.setBackground(BUTTON_BG);
    }
    
    private void updateTotals() {
        double total = orderItems.stream().mapToDouble(OrderItem::getOrderTotal).sum();
        totalLabel.setText("TOTAL: ₱" + String.format("%.2f", total));
    }
    
    private void processPayment() {
        try {
            double amount = Double.parseDouble(amountInput.getText());
            double total = orderItems.stream().mapToDouble(OrderItem::getOrderTotal).sum();
            
            if (amount < total) {
                JOptionPane.showMessageDialog(this, "Amount received is less than total.", 
                                            "Insufficient Payment", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double change = amount - total;
            
            receivedLabel.setText("RECEIVED: ₱" + String.format("%.2f", amount));
            changeLabel.setText("CHANGE: ₱" + String.format("%.2f", change));
            
            // Show receipt dialog
            ReceiptDialog receiptDialog = new ReceiptDialog(this, customerInput.getText(), 
                new ArrayList<>(orderItems), total, amount, change);
            receiptDialog.setBackClickCallback(this::handleReceiptBack);
            receiptDialog.setVisible(true);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount");
        }
    }
    
    private void handleReceiptBack() {
    // Calculate totals for the sale record
    double subtotal = orderItems.stream().mapToDouble(OrderItem::getOrderTotal).sum();
    double total = subtotal; // Assuming no tax for now
    double received = Double.parseDouble(receivedLabel.getText().replace("RECEIVED: ₱", ""));
    double change = Double.parseDouble(changeLabel.getText().replace("CHANGE: ₱", ""));
    
    // DEBUG: Print order items count
    System.out.println("DEBUG: Number of order items: " + orderItems.size());
    
    // Create order items list for the sale record
    List<String> orderItemsList = new ArrayList<>();
    for (OrderItem item : orderItems) {
        String itemName = item.getOrderName();
        int quantity = item.getOrderQty();
        
        // DEBUG: Print each item
        System.out.println("DEBUG: Processing item - Name: " + itemName + ", Qty: " + quantity + ", Total: " + item.getOrderTotal());
        
        // Add each item with its quantity properly formatted
        if (quantity > 1) {
            orderItemsList.add(itemName + " " + quantity + "x");
        } else {
            orderItemsList.add(itemName + " 1x");
        }
    }
    
    // DEBUG: Print final order list
    System.out.println("DEBUG: Final order items list: " + orderItemsList);
    System.out.println("DEBUG: Subtotal: " + subtotal + ", Total: " + total + ", Change: " + change);
    
    // Save the sale record to CSV
    String customerName = customerInput.getText().trim();
    if (customerName.isEmpty() || customerName.equals("Customer Name")) {
        customerName = "Walk-in";
    }
    
    System.out.println("DEBUG: Customer name: " + customerName);
    
    // Call the static method to add sale record
    SalesRecordWindow.addSaleRecord(customerName, orderItemsList, subtotal, total, change);
    
    // Add to previous orders display
    List<String> itemNames = new ArrayList<>();
    for (OrderItem item : orderItems) {
        itemNames.add(item.getOrderName() + " x" + item.getOrderQty());
    }
    PreviousOrderItem prevOrder = new PreviousOrderItem(customerName, itemNames);
    previousOrders.add(prevOrder);
    previousListPanel.add(prevOrder);
    previousListPanel.revalidate();
    previousListPanel.repaint();
    
    // Reset for next order
    resetOrder();
}   
    
    private void resetOrder() {
        orderItems.clear();
        payAmounts.clear();
        orderListPanel.removeAll();
        orderListPanel.revalidate();
        orderListPanel.repaint();
        
        customerInput.setText("");
        amountInput.setText("");
        totalLabel.setText("TOTAL: ₱0");
        receivedLabel.setText("RECEIVED: ₱0");
        changeLabel.setText("CHANGE: ₱0");
        
        resetCurrentOrder();
    }
    
    private void showSalesRecord() {
        
    try {
        SalesRecordWindow salesWindow = new SalesRecordWindow(this);
        salesWindow.setVisible(true);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Could not open sales record window: " + e.getMessage(), 
                                    "Error", JOptionPane.WARNING_MESSAGE);
    }
}
    
    @Override
    public void onDeleteClicked(OrderItem item) {
        orderItems.remove(item);
        orderListPanel.remove(item);
        orderListPanel.revalidate();
        orderListPanel.repaint();
        updateTotals();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
               // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new CafePOSApp().setVisible(true);
        });
    }
}