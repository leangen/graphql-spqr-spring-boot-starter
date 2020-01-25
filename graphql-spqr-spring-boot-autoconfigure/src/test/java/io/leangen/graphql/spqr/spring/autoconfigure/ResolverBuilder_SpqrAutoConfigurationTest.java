package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.spqr.spring.test.ResolverBuilder_TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseAutoConfiguration.class, ResolverBuilder_TestConfig.class})
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
        assertNotNull(schema);
        //Operations sources wired in different ways
        // -using the default resolver builder
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromAnnotatedSource_wiredAsComponent"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromAnnotatedSource_wiredAsBean"));

        //Operations source wired as bean
        // -using additional global resolver builder
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byCustomGlobalResolverBuilder"));
        // -using default resolver builders
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byMethodName"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byAnnotation"));
        // -using custom resolver builders
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byStringQualifiedCustomResolverBuilder_wiredAsBean"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byStringQualifiedCustomResolverBuilder_wiredAsComponent"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byAnnotationQualifiedCustomResolverBuilder_wiredAsBean"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byAnnotationQualifiedCustomResolverBuilder_wiredAsComponent"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byNamedCustomResolverBuilder_wiredAsBean"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byNamedCustomResolverBuilder_wiredAsComponent"));

        //Operations source wired as component
        // -using additional global resolver builder
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byCustomGlobalResolverBuilder"));
        // -using default resolver builders
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byMethodName"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byAnnotation"));
        // -using custom resolver builders
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byStringQualifiedCustomResolverBuilder_wiredAsBean"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byStringQualifiedCustomResolverBuilder_wiredAsComponent"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byAnnotationQualifiedCustomResolverBuilder_wiredAsBean"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byAnnotationQualifiedCustomResolverBuilder_wiredAsComponent"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byNamedCustomResolverBuilder_wiredAsBean"));
        assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byNamedCustomResolverBuilder_wiredAsComponent"));
        assertNotNull(schema.getQueryType().getFieldDefinition("springPageComponent_users"));
    }

}

