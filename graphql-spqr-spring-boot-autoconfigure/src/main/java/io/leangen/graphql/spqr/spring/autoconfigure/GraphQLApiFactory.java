package io.leangen.graphql.spqr.spring.autoconfigure;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.annotations.WithResolverBuilder;
import io.leangen.graphql.spqr.spring.annotations.WithResolverBuilders;
import io.leangen.graphql.spqr.spring.web.dto.ResolverBuilderBeanIdentity;
import io.leangen.graphql.spqr.spring.web.dto.SpqrBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * GraphQL API factory to perform a search among GraphQL components.
 *
 * @author Sergei Visotsky
 * @since 0.0.5
 */
public class GraphQLApiFactory {

    private static final String VALUE = "value";

    private final ConfigurableApplicationContext context;

    public GraphQLApiFactory(ConfigurableApplicationContext context) {
        this.context = context;
    }

    public <T> T findQualifiedBeanByType(Class<? extends T> type, String qualifierValue, Class<? extends Annotation> qualifierType) {
        final NoSuchBeanDefinitionException noSuchBeanDefinitionException = new NoSuchBeanDefinitionException(qualifierValue, "No matching " + type.getSimpleName() +
                " bean found for qualifier " + qualifierValue + " of type " + qualifierType.getSimpleName() + " !");
        try {

            if (StringUtils.isEmpty(qualifierValue)) {
                if (qualifierType.equals(Qualifier.class)) {
                    return Optional.of(
                            context.getBean(type))
                            .orElseThrow(() -> noSuchBeanDefinitionException);
                }
                return context.getBean(
                        Arrays.stream(context.getBeanNamesForAnnotation(qualifierType))
                                .filter(beanName -> type.isInstance(context.getBean(beanName)))
                                .findFirst()
                                .orElseThrow(() -> noSuchBeanDefinitionException),
                        type);
            }

            return BeanFactoryAnnotationUtils.qualifiedBeanOfType(context.getBeanFactory(), type, qualifierValue);
        } catch (NoSuchBeanDefinitionException noBeanException) {
            ConfigurableListableBeanFactory factory = context.getBeanFactory();

            for (String name : factory.getBeanDefinitionNames()) {
                BeanDefinition bd = factory.getBeanDefinition(name);

                if (bd.getSource() instanceof StandardMethodMetadata) {
                    StandardMethodMetadata metadata = (StandardMethodMetadata) bd.getSource();

                    if (metadata.getReturnTypeName().equals(type.getName())) {
                        Map<String, Object> attributes = metadata.getAnnotationAttributes(qualifierType.getName());
                        if (null != attributes) {
                            if (qualifierType.equals(Qualifier.class)) {
                                if (qualifierValue.equals(attributes.get(VALUE))) {
                                    return context.getBean(name, type);
                                }
                            }
                            return context.getBean(name, type);
                        }
                    }
                }
            }

            throw noSuchBeanDefinitionException;
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<SpqrBean> findGraphQLApiBeans() {
        ConfigurableListableBeanFactory factory = context.getBeanFactory();

        List<SpqrBean> spqrBeans = new ArrayList<>();

        for (String beanName : factory.getBeanDefinitionNames()) {
            BeanDefinition bd = factory.getBeanDefinition(beanName);

            if (bd.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) bd.getSource();

                Map<String, Object> attributes = metadata.getAnnotationAttributes(GraphQLApi.class.getName());
                if (null == attributes) {
                    continue;
                }

                SpqrBean spqrBean = new SpqrBean(context, beanName, metadata.getIntrospectedMethod().getAnnotatedReturnType());

                Map<String, Object> withResolverBuildersAttributes = metadata.getAnnotationAttributes(WithResolverBuilders.class.getTypeName());
                if (withResolverBuildersAttributes != null) {
                    AnnotationAttributes[] annotationAttributesArray = (AnnotationAttributes[]) withResolverBuildersAttributes.get(VALUE);
                    Arrays.stream(annotationAttributesArray)
                            .forEach(annotationAttributes ->
                                    spqrBean.getResolverBuilders().add(
                                            new ResolverBuilderBeanIdentity(
                                                    (Class<? extends ResolverBuilder>) annotationAttributes.get(VALUE),
                                                    (String) annotationAttributes.get("qualifierValue"),
                                                    (Class<? extends Annotation>) annotationAttributes.get("qualifierType"))
                                    )
                            );
                } else {
                    Map<String, Object> withResolverBuilderAttributes = metadata.getAnnotationAttributes(WithResolverBuilder.class.getTypeName());
                    if (withResolverBuilderAttributes != null) {
                        spqrBean.getResolverBuilders().add(
                                new ResolverBuilderBeanIdentity(
                                        (Class<? extends ResolverBuilder>) withResolverBuilderAttributes.get(VALUE),
                                        (String) withResolverBuilderAttributes.get("qualifierValue"),
                                        (Class<? extends Annotation>) withResolverBuilderAttributes.get("qualifierType"))
                        );
                    }
                }

                spqrBeans.add(spqrBean);
            }
        }

        return spqrBeans;
    }

    public Map<String, SpqrBean> findGraphQLApiComponents() {
        final Map<String, Object> operationSourcesBeans = context.getBeansWithAnnotation(GraphQLApi.class);

        Map<String, SpqrBean> result = new HashMap<>();
        for (String beanName : operationSourcesBeans.keySet()) {
            Class<?> operationSourceBeanClass = operationSourcesBeans.get(beanName).getClass();
            result.put(beanName, new SpqrBean(context, beanName, GenericTypeReflector.annotate(ClassUtils.getUserClass(operationSourceBeanClass))));

            if (operationSourceBeanClass.isAnnotationPresent(WithResolverBuilder.class)) {
                WithResolverBuilder withResolverBuilder = operationSourceBeanClass.getAnnotation(WithResolverBuilder.class);
                result.get(beanName).getResolverBuilders().add(new ResolverBuilderBeanIdentity(withResolverBuilder.value(), withResolverBuilder.qualifierValue(), withResolverBuilder.qualifierType()));
            } else if (operationSourceBeanClass.isAnnotationPresent(WithResolverBuilders.class)) {
                for (WithResolverBuilder withResolverBuilder : operationSourceBeanClass.getAnnotation(WithResolverBuilders.class).value()) {
                    result.get(beanName).getResolverBuilders().add(new ResolverBuilderBeanIdentity(withResolverBuilder.value(), withResolverBuilder.qualifierValue(), withResolverBuilder.qualifierType()));
                }

            }
        }
        return result;
    }
}
