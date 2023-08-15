package io.leangen.graphql.spqr.spring.autoconfigure;

import java.util.Scanner;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.diff.DiffSet;
import graphql.schema.diff.SchemaDiff;
import graphql.schema.diff.reporting.CapturingReporter;
import graphql.schema.idl.*;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.spqr.spring.test.ResolverBuilder_TestConfig;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { BaseAutoConfiguration.class, ResolverBuilder_TestConfig.class, FileUploadAutoConfiguration.class })
@TestPropertySource(locations = "classpath:application.properties")
public class ResolverBuilder_SpqrAutoConfigurationTest {

    @Autowired
    private SpqrProperties spqrProperties;
    @Autowired
    private GraphQLSchemaGenerator schemaGenerator;
    @Autowired
    private GraphQLSchema schema;

    @Test
    public void propertiesLoad() {
        assertNotNull(spqrProperties);
        assertNotNull(spqrProperties.getBasePackages());
        assertEquals(1, spqrProperties.getBasePackages().length);
        assertEquals("com.bogus.package", spqrProperties.getBasePackages()[0]);
    }

    @Test
    public void schemaGeneratorConfigTest() {
        assertNotNull(schemaGenerator);
    }

    @Test
    public void schemaConfigTest() {
        printSchema();

        String expectedSchemaString = new Scanner(ResolverBuilder_SpqrAutoConfigurationTest.class
                .getResourceAsStream("/schema.graphql"), "UTF-8")
                .useDelimiter("\\A")
                .next();

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry reg = schemaParser.parse(expectedSchemaString);
        SchemaGenerator gen = new SchemaGenerator();

        RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(FileUploadHandler.FILE_UPLOAD_SCALAR);

        GraphQLSchema expected = gen.makeExecutableSchema(reg, runtimeWiring.build());

        diff(expected, schema);
        diff(schema, expected);
    }

    private void printSchema() {
        SchemaPrinter schemaPrinter = new SchemaPrinter(SchemaPrinter.Options.defaultOptions()
                .includeDirectives(false)
                .includeScalarTypes(true)
                .includeSchemaDefinition(true)
                .includeIntrospectionTypes(false));
        System.out.println("Augmented Schema:");
        System.out.println(schemaPrinter.print(schema));
    }

    private void diff(GraphQLSchema augmentedSchema, GraphQLSchema expected) {
        DiffSet diffSet = DiffSet.diffSet(augmentedSchema, expected);
        CapturingReporter capture = new CapturingReporter();
        new SchemaDiff(SchemaDiff.Options.defaultOptions())
                .diffSchema(diffSet, capture);
        Assertions.assertThat(capture.getDangers()).isEmpty();
        Assertions.assertThat(capture.getBreakages()).isEmpty();
    }
}

