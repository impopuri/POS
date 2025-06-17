package cafepossystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
    
public class LoginWindow extends JFrame {
    private static final Map<String, String> users = new HashMap<>();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private CafePOSApp mainWindow;
    
    static {
        users.put("admin", "password123");
        users.put("student", "cosc90");
        users.put("manager", "cafe2024");
        users.put("cashier", "pos123");
    }
    
    public LoginWindow() {
        initializeComponents();
        setupLayout();
        setupStyling();
    }
    
    private void initializeComponents() {
        setTitle("Cafe Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // Set background gradient
        setContentPane(new GradientPanel());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(60, 60, 60, 60));
        
        // Title Label
        JLabel titleLabel = new JLabel(" CAFE LOGIN ");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(245, 222, 179)); // #F5DEB3
        titleLabel.setBorder(new EmptyBorder(30, 0, 50, 0));
        
        // Username components
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        usernameLabel.setForeground(new Color(245, 222, 179));
        usernameLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
        
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(new Color(245, 222, 179));
        usernameField.setForeground(new Color(74, 74, 74));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 3),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        
        // Password components
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        passwordLabel.setForeground(new Color(245, 222, 179));
        passwordLabel.setBorder(new EmptyBorder(20, 0, 8, 0));
        
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBackground(new Color(245, 222, 179));
        passwordField.setForeground(new Color(74, 74, 74));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 3),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        
        // Login Button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(210, 105, 30)); // #D2691E
        loginButton.setPreferredSize(new Dimension(200, 80));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        loginButton.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(255, 140, 0)); // #FF8C00
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(210, 105, 30));
            }
        });
        
        // Add action listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkLogin();
            }
        });
        
        // Add Enter key functionality
        getRootPane().setDefaultButton(loginButton);
        
        // Add components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(usernameLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(usernameField);
        mainPanel.add(passwordLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(loginButton);
        
        // Add login hints panel
        JPanel hintsPanel = createLoginHintsPanel();
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(hintsPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createLoginHintsPanel() {
        JPanel hintsPanel = new JPanel();
        hintsPanel.setOpaque(false);
        hintsPanel.setLayout(new BoxLayout(hintsPanel, BoxLayout.Y_AXIS));
        
        
        
        
        
        return hintsPanel;
    }
    
    private void setupStyling() {
        // Set look and feel to system default for better appearance
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel()); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void checkLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate input
        if (username.isEmpty()) {
            showErrorMessage("Please enter a username.");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showErrorMessage("Please enter a password.");
            passwordField.requestFocus();
            return;
        }
        
        if (users.containsKey(username) && users.get(username).equals(password)) {
            // Success - show welcome message
            JOptionPane.showMessageDialog(
                this,
                "☕ Welcome " + username + "! ☕\n\nAccessing Cafe POS System...",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Hide login window
            this.setVisible(false);
            
            // Create and show main POS window
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        mainWindow = new CafePOSApp();
                        mainWindow.setVisible(true);
                        
                        // Dispose the login window to free memory
                        LoginWindow.this.dispose();
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                            null,
                            "Error loading POS System: " + e.getMessage(),
                            "System Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                        
                        // Show login window again if there's an error
                        LoginWindow.this.setVisible(true);
                    }
                }
            });
            
        } else {
            // Error message for invalid credentials
            showErrorMessage("❌ Incorrect username or password.\n\nPlease try again!");
            
            // Clear password field and focus username
            passwordField.setText("");
            usernameField.requestFocus();
            usernameField.selectAll();
        }
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Login Failed",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    // Custom panel for gradient background
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int w = getWidth(), h = getHeight();
            
            // Create gradient from brown to darker brown
            Color color1 = new Color(139, 69, 19);    // Saddle Brown
            Color color2 = new Color(160, 82, 45);    // Sienna
            Color color3 = new Color(101, 67, 33);    // Dark Brown
            
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, h/2, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h/2);
            
            gp = new GradientPaint(0, h/2, color2, 0, h, color3);
            g2d.setPaint(gp);
            g2d.fillRect(0, h/2, w, h/2);
        }
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginWindow().setVisible(true);
            }
        });
    }
}