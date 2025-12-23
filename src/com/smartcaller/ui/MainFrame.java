package com.smartcaller.ui;

import com.smartcaller.model.Call;
import com.smartcaller.model.VoiceCall;
import com.smartcaller.model.VideoCall;
import com.smartcaller.model.EmergencyCall;
import com.smartcaller.service.CallManager;
import com.smartcaller.service.EmailService;
import com.smartcaller.ui.styles.Colors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final CallManager callManager;
    private JTable callsTable;
    private CallTableModel tableModel;
    private JLabel statusLabel;

    public MainFrame() {
        this.callManager = new CallManager();
        initializeFrame();
        createComponents();
        refreshData();
    }

    private void initializeFrame() {
        setTitle("Smart Caller & Scheduler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
    }

    private Image createAppIcon() {
        int size = 32;
        java.awt.Image icon = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) icon.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Colors.PRIMARY);
        g2d.fillRoundRect(4, 4, size-8, size-8, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(8, 8, size-16, size-16, 4, 4);

        g2d.dispose();
        return icon;
    }

    private void createComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 245, 250));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("📞 Smart Caller & Scheduler");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("System Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        tableModel = new CallTableModel(callManager.getAllCallsFromDatabase());
        callsTable = new JTable(tableModel);

        // Style the table
        callsTable.setRowHeight(35);
        callsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        callsTable.setSelectionBackground(new Color(220, 237, 255));
        callsTable.setGridColor(new Color(200, 200, 200));

        // Style header
        callsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        callsTable.getTableHeader().setBackground(new Color(52, 152, 219));
        callsTable.getTableHeader().setForeground(Color.WHITE);

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < callsTable.getColumnCount(); i++) {
            callsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(callsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        buttonPanel.setBackground(new Color(240, 245, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create all buttons matching console options
        buttonPanel.add(createStyledButton("📅 Schedule New Call", new Color(46, 204, 113)));
        buttonPanel.add(createStyledButton("👁️ View Next Call", new Color(52, 152, 219)));
        buttonPanel.add(createStyledButton("✅ Process Next Call", new Color(241, 196, 15)));
        buttonPanel.add(createStyledButton("📋 View Upcoming Calls", new Color(155, 89, 182)));
        buttonPanel.add(createStyledButton("📊 View Call History", new Color(230, 126, 34)));
        buttonPanel.add(createStyledButton("🗃️ View All Calls", new Color(231, 76, 60)));
        buttonPanel.add(createStyledButton("⏪ Undo Last Action", new Color(149, 165, 166)));
        buttonPanel.add(createStyledButton("⏩ Redo Last Action", new Color(149, 165, 166)));
        buttonPanel.add(createStyledButton("📧 Send Email", new Color(22, 160, 133)));
        buttonPanel.add(createStyledButton("🚪 Exit", new Color(192, 57, 43)));

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(darkenColor(bgColor), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(brightenColor(bgColor));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        // Add action listeners based on button text
        if (text.contains("Schedule")) button.addActionListener(e -> scheduleNewCall());
        else if (text.contains("View Next")) button.addActionListener(e -> viewNextCall());
        else if (text.contains("Process Next")) button.addActionListener(e -> processNextCall());
        else if (text.contains("Upcoming")) button.addActionListener(e -> viewUpcomingCalls());
        else if (text.contains("Call History")) button.addActionListener(e -> viewCallHistory());
        else if (text.contains("All Calls")) button.addActionListener(e -> displayAllCallsFromDatabase());
        else if (text.contains("Undo")) button.addActionListener(e -> undoLastAction());
        else if (text.contains("Redo")) button.addActionListener(e -> redoLastAction());
        else if (text.contains("Email")) button.addActionListener(e -> sendEmail());
        else if (text.contains("Exit")) button.addActionListener(e -> exitApplication());

        return button;
    }

    private Color darkenColor(Color color) {
        return new Color(
                Math.max(color.getRed() - 30, 0),
                Math.max(color.getGreen() - 30, 0),
                Math.max(color.getBlue() - 30, 0)
        );
    }

    private Color brightenColor(Color color) {
        return new Color(
                Math.min(color.getRed() + 30, 255),
                Math.min(color.getGreen() + 30, 255),
                Math.min(color.getBlue() + 30, 255)
        );
    }

    // 🎯 CORE FUNCTIONALITY METHODS (Matching Console Options)

    private void scheduleNewCall() {
        ScheduleCallDialog dialog = new ScheduleCallDialog(this, callManager);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            refreshData();
            statusLabel.setText("✅ New call scheduled successfully!");
        }
    }

    private void viewNextCall() {
        Call nextCall = callManager.getNextCall();
        if (nextCall != null) {
            String message = "➡️ NEXT SCHEDULED CALL\n\n" +
                    "Contact: " + nextCall.getContactName() + "\n" +
                    "Phone: " + nextCall.getPhoneNumber() + "\n" +
                    "Time: " + nextCall.getScheduledTime() + "\n" +
                    "Type: " + nextCall.getCallType() + "\n" +
                    "Priority: " + nextCall.getPriority() + "\n" +
                    "Status: " + nextCall.getStatus();
            JOptionPane.showMessageDialog(this, message, "Next Call", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No pending calls scheduled!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void processNextCall() {
        Call nextCall = callManager.getNextCall();
        if (nextCall != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "PROCESS NEXT CALL\n\n" +
                            "Contact: " + nextCall.getContactName() + "\n" +
                            "Phone: " + nextCall.getPhoneNumber() + "\n" +
                            "Time: " + nextCall.getScheduledTime() + "\n\n" +
                            "Mark this call as COMPLETED?",
                    "Process Call",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                callManager.processNextCall();
                refreshData();
                statusLabel.setText("✅ Call processed: " + nextCall.getContactName());
                JOptionPane.showMessageDialog(this, "Call marked as COMPLETED!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No pending calls to process!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewUpcomingCalls() {
        List<Call> upcomingCalls = callManager.getUpcomingCalls();
        if (upcomingCalls.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No upcoming calls!", "Upcoming Calls", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder message = new StringBuilder("📅 UPCOMING CALLS\n\n");
            for (int i = 0; i < upcomingCalls.size(); i++) {
                Call call = upcomingCalls.get(i);
                message.append(String.format("%d. %s - %s (%s) - Priority: %d\n",
                        i + 1, call.getContactName(), call.getScheduledTime(),
                        call.getCallType().replace("_CALL", ""), call.getPriority()));
            }
            JOptionPane.showMessageDialog(this, message.toString(), "Upcoming Calls", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewCallHistory() {
        String phoneNumber = JOptionPane.showInputDialog(this,
                "Enter phone number to view call history:",
                "View Call History",
                JOptionPane.QUESTION_MESSAGE);

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            List<Call> history = callManager.getCallHistory(phoneNumber.trim());
            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No call history found for: " + phoneNumber, "Call History", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder message = new StringBuilder("📊 CALL HISTORY: " + phoneNumber + "\n\n");
                for (int i = 0; i < history.size(); i++) {
                    Call call = history.get(i);
                    message.append(String.format("%d. %s - %s (%s) - %s\n",
                            i + 1, call.getContactName(), call.getScheduledTime(),
                            call.getCallType().replace("_CALL", ""), call.getStatus()));
                }
                JOptionPane.showMessageDialog(this, message.toString(), "Call History", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void displayAllCallsFromDatabase() {
        List<Call> allCalls = callManager.getAllCallsFromDatabase();
        if (allCalls.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No calls found in database!", "All Calls", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder message = new StringBuilder("🗃️ ALL CALLS IN DATABASE\n\n");
            message.append(String.format("%-3s %-15s %-12s %-18s %-12s %-8s %-10s\n",
                    "ID", "Contact", "Phone", "Scheduled Time", "Type", "Priority", "Status"));
            message.append("─".repeat(90)).append("\n");

            for (Call call : allCalls) {
                message.append(String.format("%-3d %-15s %-12s %-18s %-12s %-8d %-10s\n",
                        call.getId(),
                        call.getContactName().length() > 15 ? call.getContactName().substring(0, 12) + "..." : call.getContactName(),
                        call.getPhoneNumber(),
                        call.getScheduledTime().toString().substring(0, 16),
                        call.getCallType().replace("_CALL", ""),
                        call.getPriority(),
                        call.getStatus()));
            }

            JTextArea textArea = new JTextArea(message.toString());
            textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(700, 400));

            JOptionPane.showMessageDialog(this, scrollPane, "All Calls in Database", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void undoLastAction() {
        if (callManager.undoLastAction()) {
            refreshData();
            statusLabel.setText("✅ Last action undone");
            JOptionPane.showMessageDialog(this, "Last action undone successfully!", "Undo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void redoLastAction() {
        if (callManager.redoLastAction()) {
            refreshData();
            statusLabel.setText("✅ Last action redone");
            JOptionPane.showMessageDialog(this, "Last action redone successfully!", "Redo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void sendEmail() {
        Call nextCall = callManager.getNextCall();
        if (nextCall != null) {
            // Show email dialog
            EmailDialog dialog = new EmailDialog(this, nextCall);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No pending calls to send email for!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?",
                "Exit Application",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void refreshData() {
        List<Call> allCalls = callManager.getAllCallsFromDatabase();
        tableModel.updateData(allCalls);

        long pendingCount = allCalls.stream()
                .filter(call -> call.getStatus() == com.smartcaller.model.CallStatus.PENDING)
                .count();

        statusLabel.setText("Total: " + allCalls.size() + " calls | Pending: " + pendingCount);
    }

    public static void main() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Look and feel error: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}