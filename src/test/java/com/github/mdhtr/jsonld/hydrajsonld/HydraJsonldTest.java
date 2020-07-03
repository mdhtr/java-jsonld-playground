package com.github.mdhtr.jsonld.hydrajsonld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import de.escalon.hypermedia.hydra.mapping.Expose;
import de.escalon.hypermedia.hydra.mapping.Term;
import de.escalon.hypermedia.hydra.mapping.Vocab;
import de.escalon.hypermedia.hydra.serialize.JacksonHydraSerializer;

class HydraJsonldTest {
	private static final String NEWLINE = System.getProperty("line.separator");
	private ObjectMapper objectMapper;
	
	@BeforeEach
	void setup() {
		objectMapper = new ObjectMapper();
		// see https://github.com/json-ld/json-ld.org/issues/76
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.registerModule(getJacksonHydraSerializerModule());
	}
	
	@Test
	@DisplayName(value = "when nothing is set, only the jackson module is used, " +
			"@context will contain the default @vocab: http://schema.org, " +
			"and the class name will become the @type of the object.")
	void nothingIsSet() throws JsonProcessingException {
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"@vocab\" : \"http://schema.org/\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"@type\" : \"Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when @Expose is used on a class, it will set the @type value")
	void exposeAnnotationOnClass() throws JsonProcessingException {
		@Expose("http://schema.org/Person")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"@vocab\" : \"http://schema.org/\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"@type\" : \"http://schema.org/Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when @Expose is used on a field or a getter, " +
			"those fields will be added to the @context")
	void exposeAnnotationOnField() throws JsonProcessingException {
		class Person {
			public String id = "http://example.com/person/1234";
			@Expose("http://schema.org/name")
			public String name = "Example Name";
			
			@Expose("http://schema.org/jobTitle")
			public String getJobTitle() {
				return "Manager";
			}
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"@vocab\" : \"http://schema.org/\"," + NEWLINE +
				"    \"name\" : \"http://schema.org/name\"," + NEWLINE +
				"    \"jobTitle\" : \"http://schema.org/jobTitle\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"@type\" : \"Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"," + NEWLINE +
				"  \"jobTitle\" : \"Manager\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	public enum Gender {
		FEMALE, MALE
	}
	
	public class Person {
		public String id = "http://example.com/person/1234";
		public String name = "Example Name";
		@Expose("http://schema.org/gender")
		public Gender gender = Gender.FEMALE;
	}
	
	@Test
	@DisplayName(value = "when @Expose is used on an Enum field, " +
			"that field will be specified in the @context as a @type: @vocab, " +
			"and a key-value pair will be added to the context resolving the Enum value " +
			"to its first letter capitalized - the rest is lowercase version")
	void exposeAnnotationOnEnumField() throws JsonProcessingException {
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"@vocab\" : \"http://schema.org/\"," + NEWLINE +
				"    \"gender\" : {" + NEWLINE +
				"      \"@id\" : \"http://schema.org/gender\"," + NEWLINE +
				"      \"@type\" : \"@vocab\"" + NEWLINE +
				"    }," + NEWLINE +
				"    \"FEMALE\" : \"Female\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"@type\" : \"Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"," + NEWLINE +
				"  \"gender\" : \"FEMALE\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when @Vocab is used on a class with a custom value, " +
			"it will overwrite the default @vocab in the @context with the custom value")
	void vocabAnnotation() throws JsonProcessingException {
		@Vocab("http://example.com/vocab/")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"@vocab\" : \"http://example.com/vocab/\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"@type\" : \"Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when @Term is used on a class, " +
			"it will set a compact IRI shorthand in the @context " +
			"that can be used when specifying identifiers with @Expose")
	void termAnnotation() throws JsonProcessingException {
		@Term(define="e", as="http://example.com/vocab/")
		@Expose("e:person")
		class Person {
			public String id = "http://example.com/person/1234";
			@Expose("e:name")
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"@vocab\" : \"http://schema.org/\"," + NEWLINE +
				"    \"e\" : \"http://example.com/vocab/\"," + NEWLINE +
				"    \"name\" : \"e:name\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"@type\" : \"e:person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	private static SimpleModule getJacksonHydraSerializerModule() {
		return new SimpleModule() {
			
			@Override
			public void setupModule(SetupContext context) {
				super.setupModule(context);
				
				context.addBeanSerializerModifier(new BeanSerializerModifier() {
					
					@Override
					public JsonSerializer<?> modifySerializer(
							SerializationConfig config,
							BeanDescription beanDesc,
							JsonSerializer<?> serializer) {
						
						if (serializer instanceof BeanSerializerBase) {
							return new JacksonHydraSerializer(
									(BeanSerializerBase) serializer);
						}
						else {
							return serializer;
						}
					}
				});
			}
		};
	}
}
