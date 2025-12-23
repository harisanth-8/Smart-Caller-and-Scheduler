package com.smartcaller.service;

import com.smartcaller.model.Call;
import javax.swing.*;
import java.awt.BorderLayout;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    /**
     * Send email notification for scheduled call
     */
    public static boolean sendEmailNotification(String toEmail, String fromEmail,
                                                String password, Call call, JFrame parentFrame) {
        // Email configuration
        String host = "smtp.gmail.com"; // For Gmail
        String port = "587";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // Show progress dialog
        JDialog progressDialog = createProgressDialog(parentFrame, "Sending email to " + call.getContactName() + "...");
        progressDialog.setVisible(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    // Create session
                    Session session = Session.getInstance(properties, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(fromEmail, password);
                        }
                    });

                    // Create message
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                    message.setSubject("📞 Call Reminder: " + call.getContactName());

                    String emailContent = createEmailContent(call);
                    message.setText(emailContent);

                    // Send message
                    Transport.send(message);
                    return true;

                } catch (Exception e) {
                    System.err.println("Email error: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(parentFrame,
                                "✅ Email sent successfully!\n\n" +
                                        "To: " + toEmail + "\n" +
                                        "Subject: Call Reminder\n" +
                                        "Contact: " + call.getContactName(),
                                "Email Sent",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(parentFrame,
                                "❌ Failed to send email!\n\n" +
                                        "Please check:\n" +
                                        "• Internet connection\n" +
                                        "• Email credentials\n" +
                                        "• SMTP settings",
                                "Email Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Email error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        return true;
    }

    private static String createEmailContent(Call call) {
        return """
                Call Reminder Notification

                You have a scheduled call:

                Contact: %s
                Phone: %s
                Scheduled Time: %s
                Call Type: %s
                Priority: %s

                Please be available for the call.

                Best regards,
                Smart Caller System""".formatted(
                call.getContactName(),
                call.getPhoneNumber(),
                call.getScheduledTime(),
                call.getCallType(),
                call.getPriority()
        );
    }

    private static JDialog createProgressDialog(JFrame parent, String message) {
        JDialog dialog = new JDialog(parent, "Sending Email", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 120);
        dialog.setLocationRelativeTo(parent);

        JLabel label = new JLabel(message, JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        dialog.add(label, BorderLayout.CENTER);
        dialog.add(progressBar, BorderLayout.SOUTH);

        return dialog;
    }
}