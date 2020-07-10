package com.github.mdhtr.jsonld.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializationTest {
	private static final String NEWLINE = System.getProperty("line.separator");
	
	public enum Gender {
		@JsonProperty("Female")
		FEMALE,
		@JsonProperty("Male")
		MALE
	}
	
	@Data
	@NoArgsConstructor
	@JsonPropertyOrder({"id", "type"})
	public static class Thing {
		@JsonProperty("@id")
		private URL id;
		@JsonProperty("@type")
		private final String type = "Thing";
		private String name;
		private String description;
	}
	
	@Data
	@NoArgsConstructor
	@JsonPropertyOrder({"context", "id", "type"})
	public static class Person {
		@JsonProperty("@context")
		@JsonPropertyOrder(alphabetic = true)
		private final Map<String, Object> context = Map.of(
				"@vocab", "http://schema.org/",
				"gender", Map.of("@type", "@vocab"),
				"knows", Map.of("@type", "@id")
		);
		@JsonProperty("@id")
		private URL id;
		@JsonProperty("@type")
		private final String type = "Person";
		private String name;
		private Gender gender;
		private URL knows;
		private Thing knowsAbout;
	}
	
	private ObjectMapper objectMapper;
	
	@BeforeEach
	void setup() {
		objectMapper = new ObjectMapper();
	}
	
	@Test
	void serializeAComplexObject() throws MalformedURLException, JsonProcessingException {
		Person person = new Person();
		person.setId(new URL("http://example.com/people/123"));
		person.setName("Example Name");
		person.setGender(Gender.FEMALE);
		person.setKnows(new URL("http://example.com/people/456"));
		Thing thing = new Thing();
		thing.setId(new URL("http://example.com/things/123"));
		thing.setName("Thing Name");
		thing.setDescription("Thing Description");
		person.setKnowsAbout(thing);
		
		String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person);
		
		assertEquals("{\n" +
				"  \"@context\" : {\n" +
				"    \"@vocab\" : \"http://schema.org/\",\n" +
				"    \"gender\" : {\n" +
				"      \"@type\" : \"@vocab\"\n" +
				"    },\n" +
				"    \"knows\" : {\n" +
				"      \"@type\" : \"@id\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"@id\" : \"http://example.com/people/123\",\n" +
				"  \"@type\" : \"Person\",\n" +
				"  \"name\" : \"Example Name\",\n" +
				"  \"gender\" : \"Female\",\n" +
				"  \"knows\" : \"http://example.com/people/456\",\n" +
				"  \"knowsAbout\" : {\n" +
				"    \"@id\" : \"http://example.com/things/123\",\n" +
				"    \"@type\" : \"Thing\",\n" +
				"    \"name\" : \"Thing Name\",\n" +
				"    \"description\" : \"Thing Description\"\n" +
				"  }\n" +
				"}", result);
	}
}
