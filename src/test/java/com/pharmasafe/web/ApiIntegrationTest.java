package com.pharmasafe.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API tests against the sample data loaded by DataSeeder.
 * Tests that create data use batches no other test asserts on, so test order does not matter.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void healthyBatchIsLowRisk() throws Exception {
        mvc.perform(get("/api/batches/PCM-2507/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("LOW"))
                .andExpect(jsonPath("$.medicine.genericName").value("Paracetamol"));
    }

    @Test
    void recalledBatchIsHighPriority() throws Exception {
        mvc.perform(get("/api/batches/azt-2402/verify"))   // lookup ignores case
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("HIGH_PRIORITY"))
                .andExpect(jsonPath("$.alerts[*].type", hasItem("RECALL")))
                .andExpect(jsonPath("$.activeRecalls", hasSize(1)));
    }

    @Test
    void lookAlikeAndExpiringSoonAreFlagged() throws Exception {
        mvc.perform(get("/api/batches/AMX-2506/verify"))
                .andExpect(jsonPath("$.riskLevel").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.alerts[*].type", hasItems("EXPIRING_SOON", "LOOK_ALIKE_NAME")));
    }

    @Test
    void unknownBatchReturns404() throws Exception {
        mvc.perform(get("/api/batches/NOPE-0000/verify"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("NOPE-0000")));
    }

    @Test
    void availabilityExcludesUnsafeStockAndPrefersArea() throws Exception {
        // Azithromycin: batch AZT-2402 is recalled, only AZT-2507 (Sri Sai, Apex) should appear.
        mvc.perform(get("/api/availability").param("medicine", "azithromycin").param("area", "LB Nagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pharmacies", hasSize(2)))
                .andExpect(jsonPath("$.pharmacies[0].area").value("LB Nagar"))
                .andExpect(jsonPath("$.pharmacies[*].batches[*].batchNumber", everyItem(is("AZT-2507"))))
                .andExpect(jsonPath("$.totalUnits").value(39));
    }

    @Test
    void availabilityNeedsMedicineParameter() throws Exception {
        mvc.perform(get("/api/availability")).andExpect(status().isBadRequest());
    }

    @Test
    void seededRecallImpactListsAffectedPharmacies() throws Exception {
        mvc.perform(get("/api/recalls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].batchNumber", hasItem("AZT-2402")));

        mvc.perform(get("/api/recalls/1/impact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.affectedPharmacies").value(3))
                .andExpect(jsonPath("$.totalUnits").value(88))
                .andExpect(jsonPath("$.recommendedActions", hasSize(4)));
    }

    @Test
    void creatingARecallReturnsImpactAndBlocksDuplicates() throws Exception {
        String body = """
                {"batchNumber":"OND-2503","reason":"Test recall","severity":"CLASS_III"}
                """;
        mvc.perform(post("/api/recalls").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.affectedPharmacies").value(1))
                .andExpect(jsonPath("$.totalUnits").value(30))
                .andExpect(jsonPath("$.recall.issuedBy").value("Manual entry"));

        mvc.perform(post("/api/recalls").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/batches/OND-2503/verify"))
                .andExpect(jsonPath("$.status").value("RECALLED"))
                .andExpect(jsonPath("$.riskLevel").value("HIGH_PRIORITY"));
    }

    @Test
    void invalidRecallRequestReturns400() throws Exception {
        mvc.perform(post("/api/recalls").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"batchNumber\":\"\",\"reason\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dashboardSummarisesTheNetwork() throws Exception {
        mvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicines").value(13))
                .andExpect(jsonPath("$.pharmacies").value(6))
                .andExpect(jsonPath("$.batches").value(19))
                .andExpect(jsonPath("$.activeRecalls", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.batchesExpiringIn30Days").value(1));
    }

    @Test
    void medicineSearchFindsByGenericName() throws Exception {
        mvc.perform(get("/api/medicines").param("query", "metformin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brandName").value("Metforal 500"));
    }
}
