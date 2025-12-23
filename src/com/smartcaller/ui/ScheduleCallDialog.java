package com.smartcaller.ui;

import com.smartcaller.model.*;
import com.smartcaller.service.CallManager;
import com.smartcaller.exception.InvalidScheduleException;
import com.smartcaller.ui.styles.Colors;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduleCallDialog extends JDialog {
    private final CallManager callManager;
    private JTextField contactField;
    private JTextField phoneField;
    private JTextField dateField;
    private JTextField timeField;
    private JComboBox<String> callTypeComboBox;
    private JSpinner prioritySpinner;
    private JTextField additionalField;
    private JLabel additionalLabel;
    private boolean success = false;

    public ScheduleCallDialog(Frame parent, CallManager callManager) {
        super(parent, "Schedule New Call - Enterprise Automation", true);
        this.callManager = callManager;
        initializeComponents();
        layoutComponents();
        setProperties();
    }

    private void initializeComponents() {
        contactField = new JTextField(20);
        phoneField = new JTextField(20);

        // Set default date/time to tomorrow
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        dateField = new JTextField(10);
        dateField.setText(tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeField = new JTextField(10);
        timeField.setText("14:30");

        // Call type with priorities
        String[] callTypes = {"Voice Call (Priority 1)", "Video Call (Priority 2)", "Emergency Call (Priority 10)"};
        callTypeComboBox = new JComboBox<>(callTypes);

        // Priority spinner (1-10)
        SpinnerNumberModel priorityModel = new SpinnerNumberModel(1, 1, 10, 1);
        prioritySpinner = new JSpinner(priorityModel);

        additionalField = new JTextField(20);
        additionalLabel = new JLabel("Additional Info:");

        // Auto-update priority based on call type
        callTypeComboBox.addActionListener(e -> updatePriorityAndFields());
    }

    private void updatePriorityAndFields() {
        String selected = (String) callTypeComboBox.getSelectedItem();
        if (selected != null) {
            switch (selected) {
                case "Voice Call (Priority 1)":
                    prioritySpinner.setValue(1);
                    additionalLabel.setText("Notes:");
                    additionalField.setText("");
                    additionalField.setVisible(true);
                    additionalLabel.setVisible(true);
                    break;
                case "Video Call (Priority 2)":
                    prioritySpinner.setValue(2);
                    additionalLabel.setText("Video Platform:");
                    additionalField.setText("Zoom");
                    additionalField.setVisible(true);
                    additionalLabel.setVisible(true);
                    break;
                case "Emergency Call (Priority 10)":
                    prioritySpinner.setValue(10);
                    additionalLabel.setText("Emergency Type:");
                    additionalField.setText("Urgent");
                    additionalField.setVisible(true);
                    additionalLabel.setVisible(true);
                    break;
            }
        }
        pack();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Colors.CARD_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Contact Name
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(createLabel("👤 Contact Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(contactField, gbc);

        // Phone Number
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(createLabel("📞 Phone Number:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(phoneField, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(createLabel("📅 Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(dateField, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(createLabel("⏰ Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(timeField, gbc);

        // Call Type
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(createLabel("🎯 Call Type:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(callTypeComboBox, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(createLabel("🚨 Priority (1-10):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(prioritySpinner, gbc);

        // Additional Field
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(additionalLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(additionalField, gbc);

        // Info Panel
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 8, 8);
        mainPanel.add(createInfoPanel(), gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Colors.CARD_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton scheduleButton = createStyledButton("🚀 Schedule Call", Colors.SUCCESS);
        scheduleButton.addActionListener(e -> scheduleCall());

        JButton cancelButton = createStyledButton("❌ Cancel", Colors.DANGER);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(scheduleButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initialize fields
        updatePriorityAndFields();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(240, 249, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 230, 255), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel infoLabel = new JLabel(
                "<html><b>Priority Guide:</b><br>" +
                        "• 1-3: Normal calls<br>" +
                        "• 4-6: Important calls<br>" +
                        "• 7-9: High priority<br>" +
                        "• 10: Emergency only</html>"
        );
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(59, 130, 246));

        infoPanel.add(infoLabel, BorderLayout.CENTER);
        return infoPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void setProperties() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void scheduleCall() {
        try {
            String contactName = contactField.getText().trim();
            String phoneNumber = phoneField.getText().trim();
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            int priority = (Integer) prioritySpinner.getValue();
            String callType = (String) callTypeComboBox.getSelectedItem();
            String additionalInfo = additionalField.getText().trim();

            // Validation
            if (contactName.isEmpty() || phoneNumber.isEmpty() || date.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!phoneNumber.matches("^[+]?[0-9]{10,15}$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid phone number (10-15 digits)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDateTime scheduledTime = LocalDateTime.parse(date + "T" + time + ":00");

            // Create appropriate call type
            Call call;
            if (callType.contains("Video Call")) {
                if (additionalInfo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter video platform!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                call = new VideoCall(contactName, phoneNumber, scheduledTime, additionalInfo);
            } else if (callType.contains("Emergency Call")) {
                if (additionalInfo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter emergency type!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                call = new EmergencyCall(contactName, phoneNumber, scheduledTime, additionalInfo);
            } else {
                call = new VoiceCall(contactName, phoneNumber, scheduledTime);
            }

            // Set priority
            call.setPriority(priority);

            callManager.scheduleCall(call);
            success = true;

            JOptionPane.showMessageDialog(this,
                    "✅ Call Scheduled Successfully!\n\n" +
                            "Contact: " + contactName + "\n" +
                            "Type: " + callType + "\n" +
                            "Priority: " + priority + "\n" +
                            "Time: " + scheduledTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) + "\n\n" +
                            "The call has been added to the automation queue.",
                    "Scheduling Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (InvalidScheduleException e) {
            JOptionPane.showMessageDialog(this, "Scheduling Error: " + e.getMessage(), "Scheduling Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date/time format! Please use:\n" +
                            "• Date: YYYY-MM-DD (e.g., 2024-12-25)\n" +
                            "• Time: HH:mm (e.g., 14:30)",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}