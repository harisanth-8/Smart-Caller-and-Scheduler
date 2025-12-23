package com.smartcaller;

import com.smartcaller.model.*;
import com.smartcaller.service.CallManager;
import com.smartcaller.exception.InvalidScheduleException;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Smart Caller & Scheduler");
        System.out.println("============================");

        // Check command line arguments
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("gui")) {
                launchGUI();
                return;
            } else if (args[0].equalsIgnoreCase("console")) {
                launchConsole();
                return;
            }
        }

        // Interactive mode selection
        String[] options = {" Graphical Interface (GUI)", "⚡ Console Interface"};
        int choice = JOptionPane.showOptionDialog(null,
                "Welcome to Smart Caller & Scheduler!\n\nChoose your interface:",
                "Interface Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            launchGUI();
        } else if (choice == 1) {
            launchConsole();
        } else {
            // User closed the dialog
            System.out.println("Application closed by user.");
            System.exit(0);
        }
    }

    private static void launchGUI() {
        System.out.println("Launching Graphical Interface...");
        try {
            // Set system look and feel - Java 25 compatible way
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // Create and show the GUI
            com.smartcaller.ui.MainFrame frame = new com.smartcaller.ui.MainFrame();
            frame.setVisible(true);
            System.out.println("✅ GUI launched successfully!");

        } catch (Exception e) {
            System.err.println("❌ GUI Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to launch GUI. Switching to console mode.\n\nError: " + e.getMessage(),
                    "GUI Launch Error",
                    JOptionPane.ERROR_MESSAGE);
            launchConsole();
        }
    }

    private static void launchConsole() {
        System.out.println("Launching Console Interface...");
        ConsoleInterface.start();
    }
}

/**
 * Console Interface - Your original console application
 */
class ConsoleInterface {
    private static final CallManager callManager = new CallManager();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void start() {
        System.out.println("=== Smart Caller & Scheduler ===");
        System.out.println("Welcome to your personal call management system!");

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    scheduleNewCall();
                    break;
                case 2:
                    viewNextCall();
                    break;
                case 3:
                    processNextCall();
                    break;
                case 4:
                    viewUpcomingCalls();
                    break;
                case 5:
                    viewCallHistory();
                    break;
                case 6:
                    displayAllCallsFromDatabase();
                    break;
                case 7:
                    undoLastAction();
                    break;
                case 8:
                    redoLastAction();
                    break;
                case 9:
                    System.out.println("Thank you for using Smart Caller & Scheduler!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. Schedule New Call");
        System.out.println("2. View Next Call");
        System.out.println("3. Process Next Call");
        System.out.println("4. View Upcoming Calls");
        System.out.println("5. View Call History by Phone Number");
        System.out.println("6. View All Calls in Database");
        System.out.println("7. Undo Last Action");
        System.out.println("8. Redo Last Action");
        System.out.println("9. Exit");
        System.out.println("=================");
    }

    private static void displayAllCallsFromDatabase() {
        System.out.println("\n--- All Calls in Database ---");
        System.out.println("Fetching all calls from database...");

        try {
            List<Call> allCalls = callManager.getAllCallsFromDatabase();

            if (allCalls.isEmpty()) {
                System.out.println("No calls found in the database!");
            } else {
                System.out.println("Total calls in database: " + allCalls.size());
                System.out.println("========================================================================================================================");
                System.out.printf("%-3s %-15s %-12s %-18s %-12s %-8s %-10s %-15s%n",
                        "ID", "Contact", "Phone", "Scheduled Time", "Type", "Priority", "Status", "Info");
                System.out.println("========================================================================================================================");

                for (Call call : allCalls) {
                    String additionalInfo = getAdditionalInfo(call);
                    System.out.printf("%-3d %-15s %-12s %-18s %-12s %-8d %-10s %-15s%n",
                            call.getId(),
                            truncateString(call.getContactName(), 15),
                            truncateString(call.getPhoneNumber(), 12),
                            call.getScheduledTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm")),
                            truncateString(call.getCallType().replace("_CALL", ""), 12),
                            call.getPriority(),
                            call.getStatus(),
                            truncateString(additionalInfo, 15));
                }
                System.out.println("========================================================================================================================");
            }
        } catch (Exception e) {
            System.out.println("Error fetching calls from database: " + e.getMessage());
        }
    }

    private static String getAdditionalInfo(Call call) {
        if (call instanceof VideoCall) {
            return "Platform: " + ((VideoCall) call).getVideoPlatform();
        } else if (call instanceof EmergencyCall) {
            return "Emergency: " + ((EmergencyCall) call).getEmergencyType();
        } else {
            return "Voice Call";
        }
    }

    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }

    private static void scheduleNewCall() {
        System.out.println("\n--- Schedule New Call ---");

        System.out.print("Enter contact name: ");
        String contactName = scanner.nextLine();

        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();

        System.out.print("Enter scheduled time (yyyy-MM-dd HH:mm): ");
        String timeInput = scanner.nextLine();

        LocalDateTime scheduledTime;
        try {
            scheduledTime = LocalDateTime.parse(timeInput, formatter);
        } catch (Exception e) {
            System.out.println("Invalid date format! Please use yyyy-MM-dd HH:mm");
            return;
        }

        System.out.println("Select call type:");
        System.out.println("1. Voice Call");
        System.out.println("2. Video Call");
        System.out.println("3. Emergency Call");
        int callType = getIntInput("Enter choice: ");

        try {
            Call call;
            switch (callType) {
                case 1:
                    call = new VoiceCall(contactName, phoneNumber, scheduledTime);
                    break;
                case 2:
                    System.out.print("Enter video platform: ");
                    String platform = scanner.nextLine();
                    call = new VideoCall(contactName, phoneNumber, scheduledTime, platform);
                    break;
                case 3:
                    System.out.print("Enter emergency type: ");
                    String emergencyType = scanner.nextLine();
                    call = new EmergencyCall(contactName, phoneNumber, scheduledTime, emergencyType);
                    break;
                default:
                    System.out.println("Invalid call type! Defaulting to Voice Call.");
                    call = new VoiceCall(contactName, phoneNumber, scheduledTime);
            }

            callManager.scheduleCall(call);

        } catch (InvalidScheduleException e) {
            System.out.println("Error scheduling call: " + e.getMessage());
        }
    }

    private static void viewNextCall() {
        System.out.println("\n--- Next Call ---");
        Call nextCall = callManager.getNextCall();
        if (nextCall != null) {
            System.out.println("Next scheduled call:");
            System.out.println(nextCall);
        } else {
            System.out.println("No calls scheduled!");
        }
    }

    private static void processNextCall() {
        System.out.println("\n--- Process Next Call ---");
        Call nextCall = callManager.getNextCall();

        if (nextCall != null) {
            System.out.println("Next call to process:");
            System.out.println(nextCall);

            System.out.print("Do you want to mark this call as COMPLETED? (yes/no): ");
            String confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("yes") || confirmation.equalsIgnoreCase("y")) {
                Call processedCall = callManager.processNextCall();
                if (processedCall != null) {
                    System.out.println("✅ Successfully processed call:");
                    System.out.println(processedCall);
                } else {
                    System.out.println("❌ Failed to process call!");
                }
            } else {
                System.out.println("Call processing cancelled.");
            }
        } else {
            System.out.println("❌ No pending calls to process!");
            System.out.println("💡 All calls may already be completed or there are no scheduled calls.");
        }
    }

    private static void viewUpcomingCalls() {
        System.out.println("\n--- Upcoming Calls ---");
        List<Call> upcomingCalls = callManager.getUpcomingCalls();
        if (upcomingCalls.isEmpty()) {
            System.out.println("No upcoming calls!");
        } else {
            System.out.println("Upcoming calls (" + upcomingCalls.size() + "):");
            for (int i = 0; i < upcomingCalls.size(); i++) {
                System.out.println((i + 1) + ". " + upcomingCalls.get(i));
            }
        }
    }

    private static void viewCallHistory() {
        System.out.println("\n--- Call History ---");
        System.out.print("Enter phone number to search: ");
        String phoneNumber = scanner.nextLine();

        List<Call> history = callManager.getCallHistory(phoneNumber);
        if (history.isEmpty()) {
            System.out.println("No call history found for: " + phoneNumber);
        } else {
            System.out.println("Call history for " + phoneNumber + ":");
            for (int i = 0; i < history.size(); i++) {
                System.out.println((i + 1) + ". " + history.get(i));
            }
        }
    }

    private static void undoLastAction() {
        System.out.println("\n--- Undo Last Action ---");
        callManager.undoLastAction();
    }

    private static void redoLastAction() {
        System.out.println("\n--- Redo Last Action ---");
        callManager.redoLastAction();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
}