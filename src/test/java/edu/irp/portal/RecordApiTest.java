package edu.irp.portal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RecordApiTest {

    @Autowired
    private MockMvc mvc;

    private static final String BODY =
        "{\"title\":\"Unit Test Record\",\"authors\":\"Tester\",\"department\":\"QA\",\"publicationYear\":2026}";

    @Test
    void seededRecordsAreListed() throws Exception {
        mvc.perform(get("/api/records"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(6)));
    }

    @Test
    void searchFindsSeededRecord() throws Exception {
        mvc.perform(get("/api/records").param("q", "crop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title", containsString("Crop")));
    }

    @Test
    void dashboardCountsEveryStatus() throws Exception {
        mvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total", greaterThanOrEqualTo(6)))
            .andExpect(jsonPath("$.byStatus.PUBLISHED", greaterThanOrEqualTo(2)));
    }

    @Test
    void viewerCannotCreate() throws Exception {
        mvc.perform(post("/api/records").contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isForbidden());
    }

    @Test
    void blankTitleIsRejected() throws Exception {
        mvc.perform(post("/api/records").header("X-Role", "SUBMITTER")
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\",\"authors\":\"x\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void fullWorkflowFromDraftToPublished() throws Exception {
        String json = mvc.perform(post("/api/records").header("X-Role", "SUBMITTER")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status", is("DRAFT")))
            .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(json, "$.id")).longValue();

        transition(id, "SUBMITTER", "SUBMITTED", 200);
        transition(id, "SUBMITTER", "APPROVED", 403);
        transition(id, "REVIEWER", "APPROVED", 200);
        transition(id, "REVIEWER", "PUBLISHED", 403);
        transition(id, "ADMIN", "PUBLISHED", 200);
        transition(id, "ADMIN", "DRAFT", 409);
    }

    private void transition(long id, String role, String to, int expected) throws Exception {
        mvc.perform(post("/api/records/" + id + "/transition").header("X-Role", role)
                .contentType(MediaType.APPLICATION_JSON).content("{\"to\":\"" + to + "\"}"))
            .andExpect(status().is(expected));
    }
}
