package com.github.mdhtr.rdf.rdf4jrio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.jupiter.api.Test;

public class Rdf4JRioJsonldTest {
	private final ValueFactory factory = SimpleValueFactory.getInstance();
	private final IRI name = factory.createIRI("http://schema.org/name");
	private final IRI knows = factory.createIRI("http://schema.org/knows");
	private final IRI schemaOrgPerson = factory.createIRI("http://schema.org/Person");
	private final IRI examplePersonId = factory.createIRI("http://example.com/person/1234");
	private final IRI exampleKnowsId = factory.createIRI("http://example.com/person/2345");
	private final Literal exampleName = factory.createLiteral("Example Name");
	private final Statement nameStatement = factory.createStatement(examplePersonId, name, exampleName);
	private final Statement typeStatement = factory.createStatement(examplePersonId, RDF.TYPE, schemaOrgPerson);
	private final Statement knowsStatement = factory.createStatement(examplePersonId, knows, exampleKnowsId);

	@Test
	void rdf4jRioJsonLd_serialize() throws IOException {
		Model model = new LinkedHashModel();
		model.add(typeStatement);
		model.add(nameStatement);
		model.add(knowsStatement);
		
		try (StringWriter w = new StringWriter()) {
			Rio.write(model, w, RDFFormat.JSONLD);
			System.out.println(w.toString());
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
	void rdf4jRioJsonLd_deserialize() throws IOException {
		InputStream inputStream = new ByteArrayInputStream(("{\n" +
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
				"}\n").getBytes());
		
		RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
		Model model = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(model));
		
		Model expectedModel = new LinkedHashModel();
		expectedModel.add(typeStatement);
		expectedModel.add(nameStatement);
		expectedModel.add(knowsStatement);
		
		try {
			Model results = Rio.parse(inputStream, "", RDFFormat.JSONLD);
			assertEquals(3, results.size());
			assertEquals(expectedModel, results);
		}
		finally {
			inputStream.close();
		}
	}
}
