package com.smartcaller.ui;

import com.smartcaller.model.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CallTableModel extends AbstractTableModel {
    private List<Call> calls;
    private final String[] columnNames = {"ID", "Contact", "Phone", "Scheduled Time", "Type", "Priority", "Status", "Info"};

    public CallTableModel(List<Call> calls) {
        this.calls = calls;
    }

    @Override
    public int getRowCount() {
        return calls.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Call call = calls.get(rowIndex);
        switch (columnIndex) {
            case 0: return call.getId();
            case 1: return call.getContactName();
            case 2: return call.getPhoneNumber();
            case 3: return call.getScheduledTime().toString().replace("T", " ");
            case 4: return call.getCallType().replace("_CALL", "");
            case 5: return getPriorityWithIcon(call.getPriority());
            case 6: return getStatusWithIcon(call.getStatus());
            case 7: return getAdditionalInfo(call);
            default: return null;
        }
    }

    private String getPriorityWithIcon(int priority) {
        if (priority == 10) return "🚨 " + priority;
        if (priority >= 7) return "⚠️ " + priority;
        if (priority >= 4) return "📞 " + priority;
        return "📱 " + priority;
    }

    private String getStatusWithIcon(CallStatus status) {
        switch (status) {
            case PENDING: return "⏳ Pending";
            case COMPLETED: return "✅ Completed";
            case MISSED: return "❌ Missed";
            default: return status.toString();
        }
    }

    private String getAdditionalInfo(Call call) {
        if (call instanceof VideoCall) {
            return "Platform: " + ((VideoCall) call).getVideoPlatform();
        } else if (call instanceof EmergencyCall) {
            return "Emergency: " + ((EmergencyCall) call).getEmergencyType();
        } else {
            return "Standard Call";
        }
    }

    public void updateData(List<Call> newCalls) {
        this.calls = newCalls;
        fireTableDataChanged();
    }
}