package com.mediq.util;

import com.mediq.dto.MedicineResponse;
import com.mediq.dto.PatientResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Component
public class ExcelGeneratorUtil {

    public byte[] generatePatientCsvReport(List<PatientResponse> patients) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("Patient ID,Full Name,Age,Gender,Blood Group,Phone,Emergency Contact,Allergies,Chronic Diseases,Registered Camp,Created At");

            for (PatientResponse p : patients) {
                writer.printf("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        clean(p.getPatientId()),
                        clean(p.getFullName()),
                        p.getAge() != null ? p.getAge() : 0,
                        clean(p.getGender()),
                        clean(p.getBloodGroup()),
                        clean(p.getPhone()),
                        clean(p.getEmergencyContact()),
                        clean(p.getAllergies()),
                        clean(p.getChronicDiseases()),
                        clean(p.getRegisteredCampTitle()),
                        p.getCreatedAt() != null ? p.getCreatedAt().toString() : ""
                );
            }
            writer.flush();
        }
        return out.toByteArray();
    }

    public byte[] generateMedicineCsvReport(List<MedicineResponse> medicines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("Medicine Code,Name,Category,Batch Number,Manufacturer,Expiry Date,Stock Quantity,Min Alert Quantity,Unit Price,Low Stock Alert");

            for (MedicineResponse m : medicines) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f,\"%s\"%n",
                        clean(m.getMedicineCode()),
                        clean(m.getName()),
                        clean(m.getCategory()),
                        clean(m.getBatchNumber()),
                        clean(m.getManufacturer()),
                        m.getExpiryDate() != null ? m.getExpiryDate().toString() : "",
                        m.getStockQuantity() != null ? m.getStockQuantity() : 0,
                        m.getMinAlertQuantity() != null ? m.getMinAlertQuantity() : 0,
                        m.getUnitPrice() != null ? m.getUnitPrice() : 0.0,
                        m.isLowStock() ? "YES" : "NO"
                );
            }
            writer.flush();
        }
        return out.toByteArray();
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\"", "\"\"");
    }
}
