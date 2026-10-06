package com.pharmasafe;

import com.pharmasafe.model.*;
import com.pharmasafe.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads SAMPLE data on first start so the demo works immediately.
 *
 * All brand names, pharmacies, phone numbers and recalls here are FICTIONAL.
 * Dates are relative to today, so the "expiring soon" and "expired" demos
 * keep working whenever the app is run.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MedicineRepository medicines;
    private final BatchRepository batches;
    private final PharmacyRepository pharmacies;
    private final InventoryItemRepository inventory;
    private final RecallRepository recalls;
    private final Clock clock;

    private final Map<String, Medicine> med = new HashMap<>();
    private final Map<String, Batch> bat = new HashMap<>();
    private final Map<String, Pharmacy> ph = new HashMap<>();

    public DataSeeder(MedicineRepository medicines, BatchRepository batches, PharmacyRepository pharmacies,
                      InventoryItemRepository inventory, RecallRepository recalls, Clock clock) {
        this.medicines = medicines;
        this.batches = batches;
        this.pharmacies = pharmacies;
        this.inventory = inventory;
        this.recalls = recalls;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (medicines.count() > 0) {
            log.info("Sample data already present, skipping seed.");
            return;
        }
        LocalDate today = LocalDate.now(clock);

        // ---- Medicines (fictional brands, real generic names) ----
        medicine("PARACIL",  "Paracil 500",  "Paracetamol",    "500 mg",     "Tablet",    "Mediora Labs",   null);
        medicine("AMOXIVIN", "Amoxivin 250", "Amoxicillin",    "250 mg",     "Capsule",   "Kavira Pharma",  "LASA-AMOXIVIN-AMLOVIN");
        medicine("AMLOVIN",  "Amlovin 5",    "Amlodipine",     "5 mg",       "Tablet",    "Kavira Pharma",  "LASA-AMOXIVIN-AMLOVIN");
        medicine("METFORAL", "Metforal 500", "Metformin",      "500 mg",     "Tablet",    "Suvarna Health", "LASA-METFORAL-METRONAL");
        medicine("METRONAL", "Metronal 400", "Metronidazole",  "400 mg",     "Tablet",    "Suvarna Health", "LASA-METFORAL-METRONAL");
        medicine("CETRIZAL", "Cetrizal 10",  "Cetirizine",     "10 mg",      "Tablet",    "Mediora Labs",   null);
        medicine("AZITHRA",  "Azithra 500",  "Azithromycin",   "500 mg",     "Tablet",    "Nirvan Pharma",  null);
        medicine("PANTORA",  "Pantora 40",   "Pantoprazole",   "40 mg",      "Tablet",    "Nirvan Pharma",  null);
        medicine("ATORVEX",  "Atorvex 10",   "Atorvastatin",   "10 mg",      "Tablet",    "Kavira Pharma",  null);
        medicine("GLIMORA",  "Glimora 2",    "Glimepiride",    "2 mg",       "Tablet",    "Suvarna Health", null);
        medicine("SALBAIR",  "Salbair",      "Salbutamol",     "100 mcg",    "Inhaler",   "Mediora Labs",   null);
        medicine("ONDEXA",   "Ondexa 4",     "Ondansetron",    "4 mg",       "Tablet",    "Nirvan Pharma",  null);
        medicine("LOSARTIA", "Losartia 50",  "Losartan",       "50 mg",      "Tablet",    "Kavira Pharma",  null);

        // ---- Pharmacies (fictional) ----
        pharmacy("CAREPLUS", "CarePlus Pharmacy",     "Tarnaka",       "+91 90000 00001");
        pharmacy("MEDPOINT", "MedPoint Chemists",     "Secunderabad",  "+91 90000 00002");
        pharmacy("SRISAI",   "Sri Sai Medicals",      "Uppal",         "+91 90000 00003");
        pharmacy("LIFELINE", "Lifeline Pharmacy",     "Kukatpally",    "+91 90000 00004");
        pharmacy("GREENX",   "GreenCross Medicals",   "Gachibowli",    "+91 90000 00005");
        pharmacy("APEX",     "Apex Medical Store",    "LB Nagar",      "+91 90000 00006");

        // ---- Batches: batch(number, medicine, months since manufacture, days until expiry, supplier) ----
        batch("PCM-2501",  "PARACIL",  10, 540, "Deccan Distributors");
        batch("PCM-2507",  "PARACIL",   3, 760, "Deccan Distributors");
        batch("AMX-2502",  "AMOXIVIN",  8, 400, "Charminar Pharma Supply");
        batch("AMX-2506",  "AMOXIVIN",  4,  12, "Charminar Pharma Supply");   // expiring soon
        batch("AML-2503",  "AMLOVIN",   7, 620, "Deccan Distributors");
        batch("MTF-2412",  "METFORAL", 14, 300, "Golconda Medisupply");
        batch("MTF-2505",  "METFORAL",  5, 700, "Golconda Medisupply");
        batch("MTR-2501",  "METRONAL", 10, 450, "Golconda Medisupply");
        batch("CTZ-2404",  "CETRIZAL", 20, -10, "Deccan Distributors");       // expired
        batch("CTZ-2508",  "CETRIZAL",  2, 680, "Deccan Distributors");
        batch("AZT-2402",  "AZITHRA",  18, 350, "Charminar Pharma Supply");   // recalled below
        batch("AZT-2507",  "AZITHRA",   3, 690, "Charminar Pharma Supply");
        batch("PNT-2503",  "PANTORA",   7,  60, "Golconda Medisupply");       // expiry notice
        batch("ATV-2502",  "ATORVEX",   8, 580, "Deccan Distributors");
        batch("GLM-2504",  "GLIMORA",   6, 610, "Golconda Medisupply");
        batch("SLB-2501",  "SALBAIR",  10, 420, "Charminar Pharma Supply");
        batch("OND-2503",  "ONDEXA",    7, 500, "Deccan Distributors");
        batch("LOS-2502",  "LOSARTIA",  8, 560, "Golconda Medisupply");
        batch("LOS-2408",  "LOSARTIA", 16, 280, "Golconda Medisupply");       // quarantined below

        bat.get("LOS-2408").setStatus(BatchStatus.QUARANTINED);

        // ---- Inventory: who holds what ----
        stock("CAREPLUS", "PCM-2501", 120); stock("CAREPLUS", "CTZ-2404", 15); stock("CAREPLUS", "AZT-2402", 30);
        stock("CAREPLUS", "MTF-2412", 90);  stock("CAREPLUS", "PNT-2503", 40); stock("CAREPLUS", "AML-2503", 60);
        // CarePlus deliberately has NO Amoxivin → availability demo

        stock("MEDPOINT", "AMX-2502", 24);  stock("MEDPOINT", "PCM-2507", 200); stock("MEDPOINT", "AZT-2402", 18);
        stock("MEDPOINT", "MTR-2501", 50);  stock("MEDPOINT", "ATV-2502", 75);  stock("MEDPOINT", "SLB-2501", 12);

        stock("SRISAI",   "AMX-2506", 7);   stock("SRISAI",   "CTZ-2508", 80);  stock("SRISAI",   "AZT-2507", 25);
        stock("SRISAI",   "GLM-2504", 45);  stock("SRISAI",   "LOS-2408", 20);

        stock("LIFELINE", "AZT-2402", 40);  stock("LIFELINE", "MTF-2505", 150); stock("LIFELINE", "OND-2503", 30);
        stock("LIFELINE", "LOS-2502", 60);

        stock("GREENX",   "AMX-2502", 10);  stock("GREENX",   "PCM-2501", 90);  stock("GREENX",   "SLB-2501", 8);

        stock("APEX",     "AZT-2507", 14);  stock("APEX",     "PNT-2503", 35);  stock("APEX",     "ATV-2502", 20);

        // ---- One sample recall already in the system ----
        Batch recalled = bat.get("AZT-2402");
        recalls.save(new Recall(recalled, "Dissolution test failure in stability sample (SAMPLE DATA)",
                RecallSeverity.CLASS_II, "State Drug Control Authority (sample)", today.minusDays(3)));
        recalled.setStatus(BatchStatus.RECALLED);

        log.info("Seeded {} medicines, {} batches, {} pharmacies, {} inventory rows (sample data).",
                medicines.count(), batches.count(), pharmacies.count(), inventory.count());
    }

    private void medicine(String key, String brand, String generic, String strength,
                          String form, String maker, String lasaGroup) {
        med.put(key, medicines.save(new Medicine(brand, generic, strength, form, maker, lasaGroup)));
    }

    private void pharmacy(String key, String name, String area, String phone) {
        ph.put(key, pharmacies.save(new Pharmacy(name, area, "Hyderabad", phone)));
    }

    private void batch(String number, String medKey, int monthsSinceMfg, int daysToExpiry, String supplier) {
        LocalDate today = LocalDate.now(clock);
        bat.put(number, batches.save(new Batch(number, med.get(medKey),
                today.minusMonths(monthsSinceMfg), today.plusDays(daysToExpiry), supplier)));
    }

    private void stock(String pharmacyKey, String batchNumber, int qty) {
        inventory.save(new InventoryItem(ph.get(pharmacyKey), bat.get(batchNumber), qty));
    }
}
