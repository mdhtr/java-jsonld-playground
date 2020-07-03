package com.github.mdhtr.rdf.rdf4jbeans;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

import com.github.kburger.rdf4j.beans.BeanMapper;
import com.github.kburger.rdf4j.beans.annotation.Predicate;
import com.github.kburger.rdf4j.beans.annotation.Subject;
import com.github.kburger.rdf4j.beans.annotation.Type;

public class Rdf4jBeansTest {
	@Data
	@NoArgsConstructor
	@Type("http://schema.org/Person")
	public static class Person {
		private String id;
		@Predicate(value = "http://schema.org/name", isLiteral = true)
		private String name;
		@Predicate(value = "http://schema.org/knows", isLiteral = false)
		private String knows;
	}
	
	@Test
	void rdf4jBeans_serialize() throws IOException {
		Person person = new Person();
		person.setId("http://example.com/person/1234");
		person.setName("Example Name");
		person.setKnows("http://example.com/person/2345");
		
		BeanMapper mapper = new BeanMapper();
		
		try (StringWriter w = new StringWriter()) {
			mapper.write(w, person, person.getId(), RDFFormat.JSONLD);
			
			assertEquals("[ {\n" +
					"  \"@id\" : \"http://example.com/person/1234\",\n" +
					"  \"@type\" : [ \"http://schema.org/Person\" ],\n" +
					"  \"http://schema.org/knows\" : [ {\n" +
					"    \"@id\" : \"http://example.com/person/2345\"\n" +
					"  } ],\n" +
					"  \"http://schema.org/name\" : [ {\n" +
					"    \"@value\" : \"Example Name\"\n" +
					"  } ]\n" +
					"} ]", w.toString());
		}
	}
	
	@Test
	void rdf4jBeans_deserialize() {
		String input = "{\n" +
				"  \"@context\": {\n" +
				"    \"@vocab\": \"http://schema.org/\",\n" +
				"    \"knows\": {\n" +
				"      \"@type\": \"@id\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"@type\": \"Person\",\n" +
				"  \"@id\": \"http://example.com/person/1234\",\n" +
				"  \"name\": \"Example Name\",\n" +
				"  \"knows\": \"http://example.com/person/2345\"\n" +
				"}";
		BeanMapper mapper = new BeanMapper();
		
		Person person = mapper.read(new StringReader(input),
				Person.class, "http://example.com/person/1234", RDFFormat.JSONLD);
		
		Person expectedPerson = new Person();
		expectedPerson.setName("Example Name");
		expectedPerson.setKnows("http://example.com/person/2345");
		
		assertEquals(person, expectedPerson);
		// todo can we get the subject somehow into the id?
	}
	
	// todo test what the @Subject annotation does. is it the @id of embedded objects?
}
