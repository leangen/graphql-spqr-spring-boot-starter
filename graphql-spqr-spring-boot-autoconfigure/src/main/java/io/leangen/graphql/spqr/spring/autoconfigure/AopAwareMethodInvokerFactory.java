package io.leangen.graphql.spqr.spring.autoconfigure;

import io.leangen.graphql.metadata.execution.Executable;
import io.leangen.graphql.metadata.execution.FixedMethodInvoker;
import io.leangen.graphql.metadata.execution.MethodInvoker;
import io.leangen.graphql.metadata.strategy.query.MethodInvokerFactory;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class AopAwareMethodInvokerFactory implements MethodInvokerFactory {

    @Override
    public Executable<Method> create(Supplier<Object> targetSupplier, Method resolverMethod, AnnotatedType enclosingType, Class<?> exposedType) {
        resolverMethod = AopUtils.selectInvocableMethod(resolverMethod, exposedType);
        return targetSupplier == null ? new MethodInvoker(resolverMethod, enclosingType) : new FixedMethodInvoker(targetSupplier, resolverMethod, enclosingType);
    }
}
