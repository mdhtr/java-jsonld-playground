package com.github.mdhtr.jsonld.jacksonjsonld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldLink;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldTypeFromJavaClass;

class JacksonJsonldSerializationTest {
	private static final String NEWLINE = System.getProperty("line.separator");
	
	private ObjectMapper objectMapper;
	
	@BeforeEach
	void setup() {
		objectMapper = new ObjectMapper();
		// this configures the JsonldModule with an empty default context
		objectMapper.registerModule(new JsonldModule());
	}
	
	@Test
	@DisplayName(value = "when no annotation is added, but only the module is registered" +
			"then nothing is added to the original json object")
	void whenOnlyTheModuleIsUsed_thenNothingHappens() throws JsonProcessingException {
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when only JsonldResource annotation is used" +
			"then nothing is added to the original json object")
	void onlyTheJsonldResourceAnnotation() throws JsonProcessingException {
		@JsonldResource
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when only JsonldNamespace annotation is used" +
			"then nothing is added to the original json object")
	void onlyTheJsonldNamespaceAnnotation() throws JsonProcessingException {
		@JsonldNamespace(name = "s", uri = "http://schema.org/")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when only JsonldType annotation is used" +
			"then the @type will be the value specified in the annotation")
	void onlyTheJsonldTypeAnnotation() throws JsonProcessingException {
		@JsonldType(value = "hello:Type")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@type\" : \"hello:Type\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when only JsonldTypeFromJavaClass annotation is used" +
			"then the @type will be the class name")
	void onlyTheJsonldTypeFromJavaClassAnnotation() throws JsonProcessingException {
		@JsonldTypeFromJavaClass
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@type\" : \"Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when only JsonldTypeFromJavaClass annotation is used" +
			"and namespace is given" +
			"then the @type will be the class name" +
			"and it will be prefixed with the namespace")
	void onlyJsonldTypeFromJavaClassWithNamespaceAnnotation() throws JsonProcessingException {
		@JsonldTypeFromJavaClass(namespace = "http://schema.org/")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@type\" : \"http://schema.org/Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when only JsonldTypeFromJavaClass annotation is used" +
			"and namespacePrefix is given" +
			"then the @type will be the class name" +
			"and it will be prefixed with the namespacePrefix followed by a colon")
	void onlyJsonldTypeFromJavaClassWithNamespacePrefixAnnotation() throws JsonProcessingException {
		@JsonldTypeFromJavaClass(namespacePrefix = "asdf")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@type\" : \"asdf:Person\"," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when JsonldNamespace and JsonldType or JsonldTypeFromJavaClass annotation is used together " +
			"then @context is added like the following: " +
			"the name from JsonldNamespace is used to set a compact IRI prefix/shorthand; " +
			"the uri from JsonldNamespace is used as the value/identifier of the a compact IRI prefix; " +
			"the @type will be the class name " +
			"and it will be prefixed with the namespace shorthand; " +
			"all field from the class are added to the context " +
			"and their values are their names prefixed with the namespace shorthand;")
	void jsonldNamespaceAndJsonldTypeFromJavaClassAnnotations() throws JsonProcessingException {
		@JsonldNamespace(name = "s", uri = "http://schema.org/")
		@JsonldTypeFromJavaClass()
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@type\" : \"Person\"," + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"s\" : \"http://schema.org/\"," + NEWLINE +
				"    \"name\" : \"s:name\"," + NEWLINE +
				"    \"id\" : \"s:id\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when JsonldId annotation is the only annotation used " +
			"then @id will be used in the JSON object instead of the field name." +
			"the field marked with @JsonldId will not be included as a term in the context ")
	void onlyJsonldIdAnnotation() throws JsonProcessingException {
		class Person {
			@JsonldId
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"name\" : \"Example Name\"," + NEWLINE +
				"  \"@id\" : \"http://example.com/person/1234\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when JsonldProperty annotation is the only annotation used " +
			"then nothing is added to the original json object")
	void onlyJsonldPropertyAnnotation() throws JsonProcessingException {
		class Person {
			public String id = "http://example.com/person/1234";
			@JsonldProperty(value = "fullName")
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when JsonldProperty annotation is used together with the JsonLdResource builder " +
			"then @context is added like the following: " +
			"all fields that are given a name with JsonldProperty will be included in the @context;  " +
			"@context will contain a mapping from field name to the value specified in the JsonldProperty annotation")
	void jsonldPropertyAnnotationAndJsonLdResourceBuilder() throws JsonProcessingException {
		class Person {
			public String id = "http://example.com/person/1234";
			@JsonldProperty(value = "fullName")
			public String name = "Example Name";
		}
		
		Person person = new Person();
		ioinformarics.oss.jackson.module.jsonld.JsonldResource jsonldResource =
				ioinformarics.oss.jackson.module.jsonld.JsonldResource.Builder.create()
						.build(person);
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"name\" : \"fullName\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonldResource));
	}
	
	@Test
	@DisplayName(value = "when JsonldResource and JsonldProperty annotations are used together " +
			"then @context is added like the following: " +
			"all fields that are given a name with JsonldProperty will be included in the @context;  " +
			"@context will contain a mapping from field name to the value specified in the JsonldProperty annotation")
	void jsonldPropertyAndJsonldResourceAnnotations() throws JsonProcessingException {
		@JsonldResource
		class Person {
			public String id = "http://example.com/person/1234";
			@JsonldProperty(value = "fullName")
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"name\" : \"fullName\"" + NEWLINE +
				"  }," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when JsonldLink annotation is the only annotation used " +
			"then nothing is added to the original json object")
	void onlyJsonldLinkAnnotation() throws JsonProcessingException {
		@JsonldLink(rel = "linkRelationName", name = "fieldName1", href = "linkUrl1")
		@JsonldLink(rel = "linkRelationName", name = "fieldName2", href = "linkUrl2")
		@JsonldLink(rel = "linkRelationName", name = "fieldName3", href = "linkUrl3")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
	
	@Test
	@DisplayName(value = "when JsonldLink annotation is used together with the JsonLdResource builder " +
			"then @context is added like the following: " +
			"@context will contain a mapping from `name` to `rel` specified in the @JasonldLink annotation " +
			"and a @type: @id will be added to them to mark the `href` values as links/IRIs, " +
			"however, the link fields with the href value are not added to the object")
	void jsonldLinkAnnotationAndJsonLdResourceBuilder() throws JsonProcessingException {
		@JsonldLink(rel = "http://example.com/vocab/link", name = "linkField1", href = "http://example.com/link1")
		@JsonldLink(rel = "http://example.com/vocab/link", name = "linkField2", href = "http://example.com/link2")
		@JsonldLink(rel = "http://example.com/vocab/link", name = "linkField3", href = "http://example.com/link3")
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		ioinformarics.oss.jackson.module.jsonld.JsonldResource jsonldResource =
				ioinformarics.oss.jackson.module.jsonld.JsonldResource.Builder.create()
						.build(person);
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"linkField1\" : {" + NEWLINE +
				"      \"@id\" : \"http://example.com/vocab/link\"," + NEWLINE +
				"      \"@type\" : \"@id\"" + NEWLINE +
				"    }," + NEWLINE +
				"    \"linkField2\" : {" + NEWLINE +
				"      \"@id\" : \"http://example.com/vocab/link\"," + NEWLINE +
				"      \"@type\" : \"@id\"" + NEWLINE +
				"    }," + NEWLINE +
				"    \"linkField3\" : {" + NEWLINE +
				"      \"@id\" : \"http://example.com/vocab/link\"," + NEWLINE +
				"      \"@type\" : \"@id\"" + NEWLINE +
				"    }" + NEWLINE +
				"  }," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonldResource));
	}
	
	@Test
	@DisplayName(value = "when JsonldResource and JsonldLink annotations are used together " +
			"then @context is added like the following: " +
			"the `name` values from the @JsonldLink will be fields in the object, " +
			"their values will be the corresponding `href` values from the annotation;  " +
			"@context will contain a mapping from `name` to `rel` specified in the @JasonldLink annotation " +
			"and a @type: @id will be added to them to mark the `href` values as links/IRIs")
	void jsonldLinkAndJsonldResourceAnnotations() throws JsonProcessingException {
		@JsonldLink(rel = "http://example.com/vocab/link", name = "linkField1", href = "http://example.com/link1")
		@JsonldLink(rel = "http://example.com/vocab/link", name = "linkField2", href = "http://example.com/link2")
		@JsonldLink(rel = "http://example.com/vocab/link", name = "linkField3", href = "http://example.com/link3")
		@JsonldResource
		class Person {
			public String id = "http://example.com/person/1234";
			public String name = "Example Name";
		}
		
		Person person = new Person();
		
		assertEquals("{" + NEWLINE +
				"  \"@context\" : {" + NEWLINE +
				"    \"linkField1\" : {" + NEWLINE +
				"      \"@id\" : \"http://example.com/vocab/link\"," + NEWLINE +
				"      \"@type\" : \"@id\"" + NEWLINE +
				"    }," + NEWLINE +
				"    \"linkField2\" : {" + NEWLINE +
				"      \"@id\" : \"http://example.com/vocab/link\"," + NEWLINE +
				"      \"@type\" : \"@id\"" + NEWLINE +
				"    }," + NEWLINE +
				"    \"linkField3\" : {" + NEWLINE +
				"      \"@id\" : \"http://example.com/vocab/link\"," + NEWLINE +
				"      \"@type\" : \"@id\"" + NEWLINE +
				"    }" + NEWLINE +
				"  }," + NEWLINE +
				"  \"id\" : \"http://example.com/person/1234\"," + NEWLINE +
				"  \"name\" : \"Example Name\"," + NEWLINE +
				"  \"linkField3\" : \"http://example.com/link3\"," + NEWLINE +
				"  \"linkField2\" : \"http://example.com/link2\"," + NEWLINE +
				"  \"linkField1\" : \"http://example.com/link1\"" + NEWLINE +
				"}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(person));
	}
}
