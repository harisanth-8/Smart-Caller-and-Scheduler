package com.smartcaller.ui;

import com.smartcaller.model.Call;
import com.smartcaller.service.EmailService;
import javax.swing.*;
import java.awt.*;

public class EmailDialog extends JDialog {
    private Call call;
    private JTextField toEmailField;
    private JTextField fromEmailField;
    private JPasswordField passwordField;
    private boolean success = false;

    public EmailDialog(JFrame parent, Call call) {
        super(parent, "Send Email Notification", true);
        this.call = call;
        initializeComponents();
        layoutComponents();
        setProperties();
    }

    private void initializeComponents() {
        toEmailField = new JTextField(25);
        fromEmailField = new JTextField(25);
        passwordField = new JPasswordField(25);

        // Set default values
        toEmailField.setText("");
        fromEmailField.setText("");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Call information
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel(
                "<html><b>Call Information:</b><br>" +
                        "Contact: " + call.getContactName() + "<br>" +
                        "Phone: " + call.getPhoneNumber() + "<br>" +
                        "Time: " + call.getScheduledTime() + "</html>");
        mainPanel.add(infoLabel, gbc);

        // Separator
        gbc.gridy = 1;
        mainPanel.add(new JSeparator(), gbc);

        // Email fields
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(new JLabel("To Email:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(toEmailField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        mainPanel.add(new JLabel("From Email:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(fromEmailField, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendButton = new JButton("📧 Send Email");
        sendButton.setBackground(new Color(46, 204, 113));
        sendButton.setForeground(Color.WHITE);
        sendButton.addActionListener(e -> sendEmail());

        JButton cancelButton = new JButton("❌ Cancel");
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setProperties() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void sendEmail() {
        String toEmail = toEmailField.getText().trim();
        String fromEmail = fromEmailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (toEmail.isEmpty() || fromEmail.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all email fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidEmail(toEmail) || !isValidEmail(fromEmail)) {
            JOptionPane.showMessageDialog(this, "Please enter valid email addresses!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean emailSent = EmailService.sendEmailNotification(toEmail, fromEmail, password, call, (JFrame) getOwner());
        if (emailSent) {
            success = true;
            dispose();
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean isSuccess() {
        return success;
    }
}