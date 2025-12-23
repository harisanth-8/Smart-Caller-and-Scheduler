package com.smartcaller.service;

import com.smartcaller.model.*;
import com.smartcaller.dao.CallDAO;
import com.smartcaller.exception.InvalidScheduleException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CallManager {
    private final CallDAO callDAO;
    private final PriorityQueue<Call> callQueue;
    private final Map<String, List<Call>> callHistory;
    private final Stack<Action> undoStack;
    private final Stack<Action> redoStack;

    public CallManager() {
        this.callDAO = new CallDAO();
        this.callQueue = new PriorityQueue<>(
                Comparator.comparing(Call::getPriority).reversed()
                        .thenComparing(Call::getScheduledTime)
        );
        this.callHistory = new HashMap<>();
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        loadCallsFromDatabase();
    }

    private void loadCallsFromDatabase() {
        try {
            List<Call> calls = callDAO.getAllCalls();

            // Only add PENDING calls to the queue
            for (Call call : calls) {
                if (call.getStatus() == CallStatus.PENDING) {
                    callQueue.offer(call);
                }

                // Build call history (include all calls for history)
                callHistory
                        .computeIfAbsent(call.getPhoneNumber(), k -> new ArrayList<>())
                        .add(call);
            }
            System.out.println("✅ Loaded " + callQueue.size() + " pending calls from database.");
        } catch (SQLException e) {
            System.err.println("Error loading calls from database: " + e.getMessage());
        }
    }

    public List<Call> getAllCallsFromDatabase() {
        try {
            return callDAO.getAllCalls();
        } catch (SQLException e) {
            System.err.println("Database error while fetching all calls: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void scheduleCall(Call call) throws InvalidScheduleException {
        try {
            int callId = callDAO.addCall(call);
            call.setId(callId);

            // Only add to queue if it's PENDING
            if (call.getStatus() == CallStatus.PENDING) {
                callQueue.offer(call);
            }

            // Update call history (include all calls)
            callHistory
                    .computeIfAbsent(call.getPhoneNumber(), k -> new ArrayList<>())
                    .add(call);

            // Push to undo stack
            undoStack.push(new Action(ActionType.ADD, call));
            redoStack.clear();

            System.out.println("Call scheduled successfully! ID: " + callId);
        } catch (SQLException e) {
            throw new InvalidScheduleException("Database error: " + e.getMessage());
        }
    }

    public Call getNextCall() {
        // Only return the next call if it's PENDING
        Call nextCall = callQueue.peek();
        if (nextCall != null && nextCall.getStatus() == CallStatus.PENDING) {
            return nextCall;
        }
        return null;
    }

    public Call processNextCall() {
        // Get the next call without removing it first
        Call nextCall = callQueue.peek();

        // Check if there's a valid PENDING call
        if (nextCall != null && nextCall.getStatus() == CallStatus.PENDING) {
            // Remove from queue
            nextCall = callQueue.poll();

            try {
                // Update status to COMPLETED in database
                callDAO.updateCallStatus(nextCall.getId(), CallStatus.COMPLETED);
                nextCall.setStatus(CallStatus.COMPLETED);

                System.out.println("✅ Call processed: " + nextCall.getContactName());
                return nextCall;

            } catch (SQLException e) {
                System.err.println("Error updating call status: " + e.getMessage());
                // If database update fails, add back to queue
                callQueue.offer(nextCall);
                return null;
            }
        } else {
            // No pending calls available
            if (nextCall != null) {
                System.out.println("⚠️ Next call is already " + nextCall.getStatus() + ": " + nextCall.getContactName());
            } else {
                System.out.println("❌ No pending calls to process!");
            }
            return null;
        }
    }

    public List<Call> getCallHistory(String phoneNumber) {
        return callHistory.getOrDefault(phoneNumber, new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(Call::getScheduledTime).reversed())
                .collect(Collectors.toList());
    }

    public List<Call> getUpcomingCalls() {
        return callQueue.stream()
                .filter(call -> call.getScheduledTime().isAfter(LocalDateTime.now()))
                .filter(call -> call.getStatus() == CallStatus.PENDING) // Only pending calls
                .sorted(Comparator.comparing(Call::getScheduledTime))
                .collect(Collectors.toList());
    }

    public List<Call> getAllPendingCalls() {
        return callQueue.stream()
                .filter(call -> call.getStatus() == CallStatus.PENDING)
                .sorted(Comparator.comparing(Call::getScheduledTime))
                .collect(Collectors.toList());
    }

    public boolean undoLastAction() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo!");
            return false;
        }

        Action lastAction = undoStack.pop();
        redoStack.push(lastAction);

        try {
            switch (lastAction.getType()) {
                case ADD:
                    callDAO.deleteCall(lastAction.getCall().getId());
                    callQueue.remove(lastAction.getCall());
                    callHistory.get(lastAction.getCall().getPhoneNumber()).remove(lastAction.getCall());
                    System.out.println("Undo: Removed scheduled call");
                    break;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error during undo: " + e.getMessage());
            return false;
        }
    }

    public boolean redoLastAction() {
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo!");
            return false;
        }

        Action lastAction = redoStack.pop();
        undoStack.push(lastAction);

        try {
            switch (lastAction.getType()) {
                case ADD:
                    int callId = callDAO.addCall(lastAction.getCall());
                    lastAction.getCall().setId(callId);
                    // Only add to queue if it was PENDING
                    if (lastAction.getCall().getStatus() == CallStatus.PENDING) {
                        callQueue.offer(lastAction.getCall());
                    }
                    callHistory
                            .computeIfAbsent(lastAction.getCall().getPhoneNumber(), k -> new ArrayList<>())
                            .add(lastAction.getCall());
                    System.out.println("Redo: Restored scheduled call");
                    break;
            }
            return true;
        } catch (SQLException | InvalidScheduleException e) {
            System.err.println("Error during redo: " + e.getMessage());
            return false;
        }
    }

    // Inner classes for undo/redo functionality
    private enum ActionType {
        ADD, UPDATE, DELETE
    }

    private static class Action {
        private final ActionType type;
        private final Call call;

        public Action(ActionType type, Call call) {
            this.type = type;
            this.call = call;
        }

        public ActionType getType() { return type; }
        public Call getCall() { return call; }
    }
}