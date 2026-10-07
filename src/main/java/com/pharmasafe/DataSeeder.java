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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Loads SAMPLE data so the demo works immediately.
 *
 * All brand names, pharmacies, phone numbers and recalls here are FICTIONAL.
 * Dates are relative to today, so the "expiring soon" and "expired" demos keep working.
 *
 * If the database already holds an older, smaller sample set, it is cleared and re-seeded.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Expected sizes of the current sample set; a database with different counts is re-seeded. */
    static final int MEDICINE_COUNT = 32;
    static final int PHARMACY_COUNT = 26;

    private final MedicineRepository medicines;
    private final BatchRepository batches;
    private final PharmacyRepository pharmacies;
    private final InventoryItemRepository inventory;
    private final RecallRepository recalls;
    private final Clock clock;

    private final Map<String, Medicine> med = new LinkedHashMap<>();
    private final Map<String, Batch> bat = new LinkedHashMap<>();
    private final Map<String, Pharmacy> ph = new LinkedHashMap<>();
    private final Set<String> stocked = new HashSet<>();   // "PHARMACY|BATCH" pairs already added

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
        if (medicines.count() == MEDICINE_COUNT && pharmacies.count() == PHARMACY_COUNT) {
            log.info("Sample data already present, skipping seed.");
            return;
        }
        if (medicines.count() > 0 || pharmacies.count() > 0) {
            log.info("Older sample data found; clearing it before re-seeding.");
            recalls.deleteAllInBatch();
            inventory.deleteAllInBatch();
            batches.deleteAllInBatch();
            pharmacies.deleteAllInBatch();
            medicines.deleteAllInBatch();
        }
        LocalDate today = LocalDate.now(clock);

        // ---- Medicines (fictional brands, real generic names) ----
        medicine("PARACIL",  "Paracil 500",     "Paracetamol",          "500 mg",          "Tablet",  "Mediora Labs",   null);
        medicine("FEBRINIL", "Febrinil 650",    "Paracetamol",          "650 mg",          "Tablet",  "Suvarna Health", null);
        medicine("AMOXIVIN", "Amoxivin 250",    "Amoxicillin",          "250 mg",          "Capsule", "Kavira Pharma",  "LASA-AMOXIVIN-AMLOVIN");
        medicine("AMLOVIN",  "Amlovin 5",       "Amlodipine",           "5 mg",            "Tablet",  "Kavira Pharma",  "LASA-AMOXIVIN-AMLOVIN");
        medicine("METFORAL", "Metforal 500",    "Metformin",            "500 mg",          "Tablet",  "Suvarna Health", "LASA-METFORAL-METRONAL");
        medicine("METRONAL", "Metronal 400",    "Metronidazole",        "400 mg",          "Tablet",  "Suvarna Health", "LASA-METFORAL-METRONAL");
        medicine("CETRIZAL", "Cetrizal 10",     "Cetirizine",           "10 mg",           "Tablet",  "Mediora Labs",   null);
        medicine("LEVOZIN",  "Levozin 5",       "Levocetirizine",       "5 mg",            "Tablet",  "Mediora Labs",   null);
        medicine("AZITHRA",  "Azithra 500",     "Azithromycin",         "500 mg",          "Tablet",  "Nirvan Pharma",  null);
        medicine("CIPROLEX", "Ciprolex 500",    "Ciprofloxacin",        "500 mg",          "Tablet",  "Nirvan Pharma",  null);
        medicine("DOXICURE", "Doxicure 100",    "Doxycycline",          "100 mg",          "Capsule", "Kavira Pharma",  null);
        medicine("PANTORA",  "Pantora 40",      "Pantoprazole",         "40 mg",           "Tablet",  "Nirvan Pharma",  null);
        medicine("OMEZAR",   "Omezar 20",       "Omeprazole",           "20 mg",           "Capsule", "Nirvan Pharma",  null);
        medicine("FAMOTIN",  "Famotin 20",      "Famotidine",           "20 mg",           "Tablet",  "Suvarna Health", null);
        medicine("ATORVEX",  "Atorvex 10",      "Atorvastatin",         "10 mg",           "Tablet",  "Kavira Pharma",  null);
        medicine("ROSUVEX",  "Rosuvex 10",      "Rosuvastatin",         "10 mg",           "Tablet",  "Kavira Pharma",  null);
        medicine("TELMIRA",  "Telmira 40",      "Telmisartan",          "40 mg",           "Tablet",  "Mediora Labs",   null);
        medicine("LOSARTIA", "Losartia 50",     "Losartan",             "50 mg",           "Tablet",  "Kavira Pharma",  null);
        medicine("CLOPIVEL", "Clopivel 75",     "Clopidogrel",          "75 mg",           "Tablet",  "Suvarna Health", null);
        medicine("GLIMORA",  "Glimora 2",       "Glimepiride",          "2 mg",            "Tablet",  "Suvarna Health", null);
        medicine("SALBAIR",  "Salbair",         "Salbutamol",           "100 mcg",         "Inhaler", "Mediora Labs",   null);
        medicine("MONTELIN", "Montelin 10",     "Montelukast",          "10 mg",           "Tablet",  "Mediora Labs",   null);
        medicine("ONDEXA",   "Ondexa 4",        "Ondansetron",          "4 mg",            "Tablet",  "Nirvan Pharma",  null);
        medicine("IBUFEN",   "Ibufen 400",      "Ibuprofen",            "400 mg",          "Tablet",  "Mediora Labs",   null);
        medicine("DICLOREN", "Dicloren 50",     "Diclofenac",           "50 mg",           "Tablet",  "Kavira Pharma",  null);
        medicine("PREDNISOL","Prednisol 10",    "Prednisolone",         "10 mg",           "Tablet",  "Nirvan Pharma",  null);
        medicine("FLUCOZEN", "Flucozen 150",    "Fluconazole",          "150 mg",          "Tablet",  "Nirvan Pharma",  null);
        medicine("COUGHEX",  "Coughex Syrup",   "Dextromethorphan",     "10 mg/5 ml",      "Syrup",   "Suvarna Health", null);
        medicine("REHYDRA",  "Rehydra ORS",     "Oral rehydration salts","21 g",           "Sachet",  "Mediora Labs",   null);
        medicine("SUNVIT",   "Sunvit D3 60K",   "Cholecalciferol",      "60,000 IU",       "Capsule", "Kavira Pharma",  null);
        medicine("NEUROVIT", "Neurovit Forte",  "Vitamin B-complex",    "Multi",           "Tablet",  "Suvarna Health", null);
        medicine("VITAZEN",  "Vitazen Multi",   "Multivitamin",         "Multi",           "Tablet",  "Mediora Labs",   null);

        // ---- Pharmacies (fictional) across Hyderabad: key, name, area, latitude, longitude ----
        pharmacy("CAREPLUS", "CarePlus Pharmacy",          "Tarnaka",       17.4302, 78.5388);
        pharmacy("LALAGUDA", "Lalaguda Medicals",          "Tarnaka",       17.4268, 78.5318);
        pharmacy("MEDPOINT", "MedPoint Chemists",          "Secunderabad",  17.4411, 78.4995);
        pharmacy("STATION",  "Station Road Chemists",      "Secunderabad",  17.4345, 78.5010);
        pharmacy("SRISAI",   "Sri Sai Medicals",           "Uppal",         17.4049, 78.5602);
        pharmacy("HEALTHUP", "HealthPlus Uppal",           "Uppal",         17.4071, 78.5574);
        pharmacy("LIFELINE", "Lifeline Pharmacy",          "Kukatpally",    17.4935, 78.4010);
        pharmacy("KPHB",     "KPHB Medicals",              "Kukatpally",    17.4840, 78.3910);
        pharmacy("GREENX",   "GreenCross Medicals",        "Gachibowli",    17.4412, 78.3501);
        pharmacy("FINDIST",  "Financial District Pharmacy","Gachibowli",    17.4180, 78.3420);
        pharmacy("APEX",     "Apex Medical Store",         "LB Nagar",      17.3465, 78.5510);
        pharmacy("SAGAR",    "Sagar Ring Road Pharmacy",   "LB Nagar",      17.3430, 78.5450);
        pharmacy("SANJIV",   "Sanjeevani Medicals",        "Dilsukhnagar",  17.3695, 78.5260);
        pharmacy("LOTUS",    "Lotus Pharmacy",             "Dilsukhnagar",  17.3672, 78.5290);
        pharmacy("WELLNESS", "Wellness First Pharmacy",    "Ameerpet",      17.4369, 78.4490);
        pharmacy("METRO",    "Metro Medicals",             "Ameerpet",      17.4385, 78.4460);
        pharmacy("CITYMED",  "CityMed Pharmacy",           "Begumpet",      17.4440, 78.4650);
        pharmacy("HILLTOP",  "Hilltop Chemists",           "Banjara Hills", 17.4148, 78.4360);
        pharmacy("JUBILEE",  "Jubilee Health Store",       "Jubilee Hills", 17.4318, 78.4085);
        pharmacy("TECHCARE", "TechCare Pharmacy",          "Madhapur",      17.4490, 78.3925);
        pharmacy("KONDA",    "Kondapur Medicals",          "Kondapur",      17.4608, 78.3612);
        pharmacy("MIYAPUR",  "Miyapur Health Point",       "Miyapur",       17.4975, 78.3625);
        pharmacy("MEHDI",    "Mehdi Medicals",             "Mehdipatnam",   17.3958, 78.4422);
        pharmacy("OLDCITY",  "Old City Pharmacy",          "Charminar",     17.3625, 78.4738);
        pharmacy("MALKA",    "Malkajgiri Medicals",        "Malkajgiri",    17.4540, 78.5278);
        pharmacy("KOMPALLY", "Kompally Pharmacy",          "Kompally",      17.5382, 78.4872);

        // ---- Demo batches with known stories: number, medicine, months since manufacture, days to expiry ----
        batch("PCM-2501",  "PARACIL",  10, 540, "Deccan Distributors");
        batch("PCM-2507",  "PARACIL",   3, 760, "Deccan Distributors");
        batch("AMX-2502",  "AMOXIVIN",  8, 400, "Charminar Pharma Supply");
        batch("AMX-2506",  "AMOXIVIN",  4,  12, "Charminar Pharma Supply");   // expiring soon + look-alike
        batch("AML-2503",  "AMLOVIN",   7, 620, "Deccan Distributors");
        batch("MTF-2412",  "METFORAL", 14, 300, "Golconda Medisupply");       // look-alike
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
        batch("OND-2503",  "ONDEXA",    7, 500, "Deccan Distributors");       // used for the recall demo
        batch("LOS-2502",  "LOSARTIA",  8, 560, "Golconda Medisupply");
        batch("LOS-2408",  "LOSARTIA", 16, 280, "Golconda Medisupply");       // quarantined below

        // ---- Two regular batches for every other medicine ----
        String[][] regular = {
                {"FBN", "FEBRINIL"}, {"LVZ", "LEVOZIN"}, {"CPX", "CIPROLEX"}, {"DXC", "DOXICURE"},
                {"OMZ", "OMEZAR"}, {"FMT", "FAMOTIN"}, {"RSV", "ROSUVEX"}, {"TLM", "TELMIRA"},
                {"CPV", "CLOPIVEL"}, {"MTL", "MONTELIN"}, {"IBU", "IBUFEN"}, {"DCL", "DICLOREN"},
                {"PRD", "PREDNISOL"}, {"FLZ", "FLUCOZEN"}, {"CGX", "COUGHEX"}, {"ORS", "REHYDRA"},
                {"SVD", "SUNVIT"}, {"NRV", "NEUROVIT"}, {"VTZ", "VITAZEN"}};
        String[] suppliers = {"Deccan Distributors", "Golconda Medisupply", "Charminar Pharma Supply"};
        for (int i = 0; i < regular.length; i++) {
            String supplier = suppliers[i % suppliers.length];
            batch(regular[i][0] + "-2504", regular[i][1], 6, 420 + 7 * i, supplier);
            batch(regular[i][0] + "-2509", regular[i][1], 1, 720 + 5 * i, supplier);
        }

        bat.get("LOS-2408").setStatus(BatchStatus.QUARANTINED);

        // ---- Fixed stock for the demo stories ----
        stock("CAREPLUS", "AZT-2402", 30);   // recalled batch held by 3 pharmacies, 88 units
        stock("MEDPOINT", "AZT-2402", 18);
        stock("LIFELINE", "AZT-2402", 40);
        stock("LIFELINE", "OND-2503", 30);   // recall demo: 1 pharmacy, 30 units
        stock("CAREPLUS", "CTZ-2404", 15);   // expired stock
        stock("SRISAI",   "LOS-2408", 20);   // quarantined stock
        stock("SRISAI",   "AMX-2506", 7);    // expiring soon
        stock("APEX",     "AZT-2507", 14);
        stock("SRISAI",   "AZT-2507", 25);

        // Paracetamol is everywhere, as in real life.
        int n = 0;
        for (String key : ph.keySet()) {
            stock(key, (n % 2 == 0) ? "PCM-2501" : "PCM-2507", 40 + (n * 37) % 160);
            n++;
        }

        // ---- Everything else: a varied but repeatable spread (fixed random seed) ----
        Set<String> demoOnly = Set.of("AZT-2402", "OND-2503", "CTZ-2404", "LOS-2408", "AMX-2506",
                "PCM-2501", "PCM-2507");
        Random random = new Random(2026);
        for (String pharmacyKey : ph.keySet()) {
            for (Map.Entry<String, Batch> e : bat.entrySet()) {
                if (demoOnly.contains(e.getKey()) || e.getValue().getStatus() != BatchStatus.ACTIVE) {
                    continue;
                }
                if (random.nextInt(100) < 35) {
                    stock(pharmacyKey, e.getKey(), 5 + random.nextInt(146));
                }
            }
        }

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

    private void pharmacy(String key, String name, String area, double latitude, double longitude) {
        String phone = String.format("+91 90000 %05d", ph.size() + 1);
        ph.put(key, pharmacies.save(new Pharmacy(name, area, "Hyderabad", phone, latitude, longitude)));
    }

    private void batch(String number, String medKey, int monthsSinceMfg, int daysToExpiry, String supplier) {
        LocalDate today = LocalDate.now(clock);
        bat.put(number, batches.save(new Batch(number, med.get(medKey),
                today.minusMonths(monthsSinceMfg), today.plusDays(daysToExpiry), supplier)));
    }

    private void stock(String pharmacyKey, String batchNumber, int qty) {
        if (stocked.add(pharmacyKey + "|" + batchNumber)) {
            inventory.save(new InventoryItem(ph.get(pharmacyKey), bat.get(batchNumber), qty));
        }
    }
}
