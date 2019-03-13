package io.leangen.graphql.spqr.spring.autoconfigure;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.spqr.spring.test.ResolverBuilder_TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpqrAutoConfiguration.class, ResolverBuilder_TestConfig.class})
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
        Assert.assertNotNull(spqrProperties);
        Assert.assertNotNull(spqrProperties.getBasePackages());
        Assert.assertEquals(1, spqrProperties.getBasePackages().length);
        Assert.assertEquals("com.bogus.package", spqrProperties.getBasePackages()[0]);
    }

    @Test
    public void schemaGeneratorConfigTest() {
        Assert.assertNotNull(schemaGenerator);
    }

    @Test
    public void schemaConfigTest() {
        Assert.assertNotNull(schema);
        //Operations sources wired in different ways
        // -using the default resolver builder
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromAnnotatedSource_wiredAsComponent"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromAnnotatedSource_wiredAsBean"));

        //Operations source wired as bean
        // -using additional global resolver builder
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byCustomGlobalResolverBuilder"));
        // -using default resolver builders
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byMethodName"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byAnnotation"));
        // -using custom resolver builders
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byStringQualifiedCustomResolverBuilder_wiredAsBean"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byStringQualifiedCustomResolverBuilder_wiredAsComponent"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byAnnotationQualifiedCustomResolverBuilder_wiredAsBean"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byAnnotationQualifiedCustomResolverBuilder_wiredAsComponent"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byNamedCustomResolverBuilder_wiredAsBean"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsBean_byNamedCustomResolverBuilder_wiredAsComponent"));

        //Operations source wired as component
        // -using additional global resolver builder
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byCustomGlobalResolverBuilder"));
        // -using default resolver builders
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byMethodName"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byAnnotation"));
        // -using custom resolver builders
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byStringQualifiedCustomResolverBuilder_wiredAsBean"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byStringQualifiedCustomResolverBuilder_wiredAsComponent"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byAnnotationQualifiedCustomResolverBuilder_wiredAsBean"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byAnnotationQualifiedCustomResolverBuilder_wiredAsComponent"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byNamedCustomResolverBuilder_wiredAsBean"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("greetingFromBeanSource_wiredAsComponent_byNamedCustomResolverBuilder_wiredAsComponent"));
        Assert.assertNotNull(schema.getQueryType().getFieldDefinition("springPageComponent_users"));
    }

}

