package bankmonitor.controller;


import bankmonitor.error.DTOError;
import bankmonitor.error.TransactionError;
import bankmonitor.model.Transaction;
import bankmonitor.model.TransactionData;
import bankmonitor.model.TransactionDataDTO;
import bankmonitor.model.TransactionV2;
import bankmonitor.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiV2Test {

    @MockBean
    TransactionService transactionService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        var transactions = LongStream
                .range(0L, 10L)
                .mapToObj(id -> {
                    TransactionV2 tr = Instancio.of(TransactionV2.class)
                            .supply(field(TransactionV2::getId), () -> id)
                            .supply(field(TransactionV2::getTimestamp), () -> LocalDateTime.now())
                            .supply(field(TransactionV2::getTransactionData), gen -> {
                                TransactionData td = Instancio.of(TransactionData.class)
                                        .set(field(TransactionData::getId), id)
                                        .create();
                                TransactionDataDTO tde = Instancio.of(TransactionDataDTO.class)
                                        .withNullable(all(String.class))
                                        .supply(field(TransactionDataDTO::getAmount), () -> gen.intRange(-1000, 1000))
                                        .create();

                                td.setDetails(tde);
                                return td;
                            })
                            .create();


                    try {
                        tr.setData(objectMapper.writeValueAsString(tr.getTransactionData()));
                    } catch (JsonProcessingException e) {
                        tr.setData("{}");
                    }
                    return tr;
                })
                .toList();


        given(transactionService.getAllTransactions()).willReturn(
                transactions.stream().map(Either::<TransactionError, TransactionV2>right).toList()
        );

        given(transactionService.findTransactionById(anyLong())).willAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return transactions
                    .stream()
                    .filter(tr -> tr.getId().equals(id))
                    .findFirst()
                    .map(Either::<TransactionError, TransactionV2>right)
                    .orElse(Either.left(new DTOError.NotFound(id)));
        });

        given(transactionService.saveTransaction(any())).willAnswer(invocation -> {
            TransactionDataDTO dto = invocation.getArgument(0);
            TransactionV2 tr2 = Instancio.of(TransactionV2.class)
                    .supply(field(TransactionV2::getId), gen -> gen.longRange(1, 1000))
                    .supply(field(TransactionV2::getTimestamp), () -> LocalDateTime.now())
                    .supply(field(TransactionV2::getData), () -> {
                        try {
                            return objectMapper.writeValueAsString(dto);
                        } catch (JsonProcessingException e) {
                            return "{}";
                        }
                    })
                    .supply(field(TransactionV2::getTransactionData), () -> {
                        return  TransactionData.builder()
                                .details(dto)
                                .build();
                    })
                    .create();
            tr2.getTransactionData().setId(tr2.getId());
            return Either.right(tr2);
        });


        given(transactionService.updateTransaction(any())).willAnswer(invocation -> {
            TransactionV2 tr2 = invocation.getArgument(0);
            return Either.right(tr2);
        });

    }

    @Test
    void testGetAllTransactions() throws Exception {
        mvc
                .perform(get("/api/v2/transactions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    public void testSaveTransaction() throws Exception {
        String create = "{\"reference\": \"new\", \"amount\": 100, \"sender\": \"Test\"}";
        mvc
                .perform(post("/api/v2/transactions").contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.reference").value("new"))
                .andExpect(jsonPath("$.data.amount").value(100))
                .andExpect(jsonPath("$.data.sender").value("Test"));
    }

    @Test
    void testUpdateTransaction() throws Exception {
        String update = "{\"reference\": \"bar\", \"amount\": 200}";
        mvc
                .perform(put("/api/v2/transactions/1").contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.reference").value("bar"))
                .andExpect(jsonPath("$.data.amount").value(200));
    }


}
