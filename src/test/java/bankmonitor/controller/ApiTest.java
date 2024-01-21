package bankmonitor.controller;


import bankmonitor.model.TransactionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void testCompatibility() throws Exception {
        // create on legacy
        String create = """
                {
                    "amount": 100,
                    "reference": "new item",
                    "reason": "compatibility test",
                    "sender": "Test"
                }
                """;

        // get result from mvc
        mvc
                .perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/v1/transactions"));

        MvcResult createResult = mvc
                .perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.reference").value("new item"))
                .andExpect(jsonPath("$.amount").value(100))
                .andReturn();

        // check main fields
        JSONObject legacyTransaction = new JSONObject(createResult.getResponse().getContentAsString());
        assertTrue(legacyTransaction.has("id"));
        assertThat(legacyTransaction.getInt("amount"), equalTo(100));
        assertThat(legacyTransaction.getString("reference"), equalTo("new item"));

        // check JSON data field
        JSONObject legacyTransactionData = new JSONObject(legacyTransaction.getString("data"));
        assertThat(legacyTransactionData.getString("reason"), equalTo("compatibility test"));
        assertThat(legacyTransactionData.getString("sender"), equalTo("Test"));

        var id = legacyTransaction.getLong("id");

        // update on v2
        String update = """
                {
                    "amount": 200,
                    "reference": "updated item"
                }
                """;

        MvcResult updateResult = mvc
                .perform(put("/api/v2/transactions/" + id).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andReturn();


        TransactionDTO dto = objectMapper.readValue(updateResult.getResponse().getContentAsString(), TransactionDTO.class);

        // read back from legacy
        MvcResult reReadResult = mvc
                .perform(get("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        // read as JSON array of transactions
        var transactionArray = new JSONArray(reReadResult.getResponse().getContentAsString());
        var reReadTransactionOpt = IntStream.range(0, transactionArray.length())
                .mapToObj(i -> Try.of(() -> transactionArray.getJSONObject(i)))
                .filter(Try::isSuccess)
                .map(Try::get)
                .filter(transaction -> transaction.optLong("id", -1L) == id)
                .findFirst();

        assertTrue(reReadTransactionOpt.isPresent());
        assertThat(reReadTransactionOpt.get().getLong("id"), equalTo(id));
        assertThat(reReadTransactionOpt.get().getInt("amount"), equalTo(200));
        assertThat(reReadTransactionOpt.get().getString("reference"), equalTo("updated item"));

    }


}
