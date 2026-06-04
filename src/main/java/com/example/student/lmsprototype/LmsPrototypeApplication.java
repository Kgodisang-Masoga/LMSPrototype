package com.example.student.lmsprototype;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class LmsPrototypeApplication {

    // Models - Made public to fix visibility warnings
    public static class LeaveRequest {
        public String refId;
        public String employeeName;
        public String department;
        public String type;
        public String startDate;
        public String endDate;
        public int days;
        public String status;

        public LeaveRequest(String refId, String employeeName, String department, String type, String startDate, String endDate, int days, String status) {
            this.refId = refId;
            this.employeeName = employeeName;
            this.department = department;
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
            this.days = days;
            this.status = status;
        }
    }

    public static class EmployeeBalance {
        public int totalAnnual = 15; public int usedAnnual = 6;
        public int totalSick = 10;   public int usedSick = 2;
        public int totalStudy = 5;   public int usedStudy = 2;
    }

    // Database Simulation
    private List<LeaveRequest> requests = new ArrayList<>();
    private EmployeeBalance johnBalance = new EmployeeBalance();
    private int nextRefCounter = 5;

    public LmsPrototypeApplication() {
        // Figma Initial Data
        requests.add(new LeaveRequest("LV-001", "John Doe", "Engineering", "Annual", "15/11/2023", "20/11/2023", 5, "PENDING"));
        requests.add(new LeaveRequest("LV-002", "John Doe", "Engineering", "Sick", "10/10/2023", "11/10/2023", 2, "APPROVED"));
        requests.add(new LeaveRequest("LV-003", "John Doe", "Engineering", "Study", "05/08/2023", "06/08/2023", 2, "APPROVED"));
        requests.add(new LeaveRequest("LV-004", "John Doe", "Engineering", "Annual", "14/07/2023", "14/07/2023", 1, "REJECTED"));

        requests.add(new LeaveRequest("#102", "Sarah Smith", "Marketing", "Sick", "01/11/23", "02/11/23", 2, "PENDING"));
        requests.add(new LeaveRequest("#103", "Marcus Lee", "Engineering", "Study", "22/11/23", "24/11/23", 3, "PENDING"));
        requests.add(new LeaveRequest("#104", "Priya Patel", "Design", "Annual", "28/11/23", "30/11/23", 3, "PENDING"));
    }

    // --- Login Authentication ---
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        Map<String, Object> response = new HashMap<>();

        if ("emp".equals(username) && "123".equals(password)) {
            response.put("success", true);
            response.put("role", "EMPLOYEE");
        } else if ("man".equals(username) && "456".equals(password)) {
            response.put("success", true);
            response.put("role", "MANAGER");
        } else if ("admin".equals(username) && "789".equals(password)) {
            response.put("success", true);
            response.put("role", "ADMIN");
        } else {
            response.put("success", false);
            response.put("message", "Invalid username or password");
        }
        return response;
    }

    // Endpoint 1: Get Employee Dashboard Data
    @GetMapping("/employee")
    public Map<String, Object> getEmployeeData() {
        Map<String, Object> response = new HashMap<>();
        response.put("balances", johnBalance);
        List<LeaveRequest> employeeHistory = new ArrayList<>();
        for (LeaveRequest req : requests) {
            if (req.employeeName.equals("John Doe")) employeeHistory.add(req);
        }
        response.put("history", employeeHistory);
        return response;
    }

    // Endpoint 2: Get Manager Dashboard Data
    @GetMapping("/manager")
    public List<LeaveRequest> getManagerData() {
        List<LeaveRequest> pendingRequests = new ArrayList<>();
        for (LeaveRequest req : requests) {
            if (req.status.equals("PENDING") && !req.employeeName.equals("John Doe")) {
                pendingRequests.add(req);
            }
        }
        return pendingRequests;
    }

    // Endpoint 3: Submit a Leave Request
    @PostMapping("/apply")
    public String applyForLeave(@RequestBody Map<String, String> payload) {
        String type = payload.get("type");
        String start = payload.get("start");
        String end = payload.get("end");
        int days = Integer.parseInt(payload.get("days"));
        if (days <= 0) return "Error: Days must be > 0";
        String newRef = "LV-00" + nextRefCounter++;
        requests.add(new LeaveRequest(newRef, "John Doe", "Engineering", type, start, end, days, "PENDING"));
        return "Success";
    }

    // Endpoint 4: Approve/Reject Leave
    @PostMapping("/process/{refId}")
    public String processLeave(@PathVariable String refId, @RequestParam boolean approve) {
        for (LeaveRequest req : requests) {
            if (req.refId.equals(refId)) {
                req.status = approve ? "APPROVED" : "REJECTED";
                if (approve && req.employeeName.equals("John Doe")) {
                    if (req.type.equals("Annual")) johnBalance.usedAnnual += req.days;
                    if (req.type.equals("Sick")) johnBalance.usedSick += req.days;
                    if (req.type.equals("Study")) johnBalance.usedStudy += req.days;
                }
                return "Processed";
            }
        }
        return "Not Found";
    }

    public static void main(String[] args) {
        SpringApplication.run(LmsPrototypeApplication.class, args);
    }
}