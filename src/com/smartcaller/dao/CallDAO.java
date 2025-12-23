package com.smartcaller.dao;

import com.smartcaller.model.*;
import com.smartcaller.util.DatabaseConnection;
import com.smartcaller.exception.InvalidScheduleException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CallDAO {

    public int addCall(Call call) throws SQLException, InvalidScheduleException {
        // Validate schedule time
        if (call.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new InvalidScheduleException("Cannot schedule call in the past");
        }

        // Validate phone number
        if (!isValidPhoneNumber(call.getPhoneNumber())) {
            throw new InvalidScheduleException("Invalid phone number format");
        }

        String sql = "INSERT INTO calls (contact_name, phone_number, scheduled_time, call_type, priority) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, call.getContactName());
            stmt.setString(2, call.getPhoneNumber());
            stmt.setTimestamp(3, Timestamp.valueOf(call.getScheduledTime()));
            stmt.setString(4, call.getCallType());
            stmt.setInt(5, call.getPriority());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        }
    }

    public List<Call> getAllCalls() throws SQLException {
        List<Call> calls = new ArrayList<>();
        String sql = "SELECT * FROM calls ORDER BY scheduled_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Call call = createCallFromResultSet(rs);
                calls.add(call);
            }
        }
        return calls;
    }

    public List<Call> getCallsByPhoneNumber(String phoneNumber) throws SQLException {
        List<Call> calls = new ArrayList<>();
        String sql = "SELECT * FROM calls WHERE phone_number = ? ORDER BY scheduled_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Call call = createCallFromResultSet(rs);
                    calls.add(call);
                }
            }
        }
        return calls;
    }

    public boolean updateCallStatus(int callId, CallStatus status) throws SQLException {
        String sql = "UPDATE calls SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, callId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCall(int callId) throws SQLException {
        String sql = "DELETE FROM calls WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, callId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Call createCallFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String contactName = rs.getString("contact_name");
        String phoneNumber = rs.getString("phone_number");
        LocalDateTime scheduledTime = rs.getTimestamp("scheduled_time").toLocalDateTime();
        String callType = rs.getString("call_type");
        int priority = rs.getInt("priority");
        CallStatus status = CallStatus.valueOf(rs.getString("status"));

        Call call;
        switch (callType) {
            case "VOICE_CALL":
                call = new VoiceCall(contactName, phoneNumber, scheduledTime);
                break;
            case "VIDEO_CALL":
                call = new VideoCall(contactName, phoneNumber, scheduledTime, "Unknown");
                break;
            case "EMERGENCY_CALL":
                call = new EmergencyCall(contactName, phoneNumber, scheduledTime, "General");
                break;
            default:
                call = new VoiceCall(contactName, phoneNumber, scheduledTime);
        }

        call.setId(id);
        call.setPriority(priority);
        call.setStatus(status);

        return call;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Simple phone number validation
        return phoneNumber != null && phoneNumber.matches("^[+]?[0-9]{10,15}$");
    }
}
