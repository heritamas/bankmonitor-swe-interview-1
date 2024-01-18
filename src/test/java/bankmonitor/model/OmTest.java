package bankmonitor.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.Data;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.Optional;

@Data
@ToString
class Book {
    String title;
    String subTitle;
}

public class OmTest {

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

    @Test
    public void testSerialize() throws JsonProcessingException {
        Book book = new Book();
        book.setTitle("Oliver Twist");
        //book.setSubTitle(Optional.of("The Parish Boy's Progress"));
        book.setSubTitle("The Parish Boy's Progress");

        System.out.println(objectMapper.writeValueAsString(book));
    }

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String json = """
                {"title": "Dune" }""";

        Book book = objectMapper.readValue(json, Book.class);

        System.out.println(book);
    }
}
