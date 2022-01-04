package io.leangen.graphql.spqr.spring.modules.data;

import io.leangen.graphql.metadata.OperationArgument;
import io.leangen.graphql.metadata.Resolver;
import io.leangen.graphql.metadata.strategy.query.PropertyOperationInfoGenerator;
import io.leangen.graphql.metadata.strategy.query.PublicResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilderParams;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

public class SpringDataRepositoryResolverBuilder extends PublicResolverBuilder {
  public SpringDataRepositoryResolverBuilder(String... basePackages) {
    super(basePackages);
    this.operationInfoGenerator = new PropertyOperationInfoGenerator();
  }

  @Override
  protected boolean isQuery(Method method, ResolverBuilderParams params) {
    return super.isQuery(method, params) && inputNotPublisherOrExample(method);
  }

  private boolean inputNotPublisherOrExample(Method method) {
    for (Class<?> cls : method.getParameterTypes()) {
      if (Publisher.class.isAssignableFrom(cls) || Example.class.isAssignableFrom(cls)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected boolean isMutation(Method method, ResolverBuilderParams params) {
    String name = method.getName(); // Spring data repository naming conventions assumed
    return (name.contains("delete") || name.contains("save") || name.contains("update"))
      && inputNotPublisherOrExample(method);
  }

  @Override
  protected boolean isSubscription(Method method, ResolverBuilderParams params) {
    return false;
  }

  @Override
  protected boolean isPackageAcceptable(Member method, ResolverBuilderParams params) {
    return true; // This ResolverBuilder is opted into through annotation, wherever it is used, it is valid
  }

  @Override
  public Collection<Resolver> buildQueryResolvers(ResolverBuilderParams params) {
    return renameOperations(super.buildQueryResolvers(params), params);
  }

  @Override
  public Collection<Resolver> buildMutationResolvers(ResolverBuilderParams params) {
    return renameOperations(super.buildMutationResolvers(params), params);
  }

  @Override
  public Collection<Resolver> buildSubscriptionResolvers(ResolverBuilderParams params) {
    return renameOperations(super.buildSubscriptionResolvers(params), params);
  }

  private Collection<Resolver> renameOperations(Collection<Resolver> resolvers, ResolverBuilderParams params) {
    Type repositoryResourceType = dataRepositoryType(params);
    return repositoryResourceType == null ? resolvers
      : resolvers.stream().map(r -> renameOperation(r, repositoryResourceType)).collect(Collectors.toList());
  }

  private Type dataRepositoryType(ResolverBuilderParams params) {
    for (Type t : params.getExposedBeanType().getGenericInterfaces()) {
      if (ParameterizedType.class.isAssignableFrom(t.getClass())
        && Repository.class.isAssignableFrom((Class<?>) ((ParameterizedType) t).getRawType())) {
        return ((ParameterizedType) t).getActualTypeArguments()[0];
      }
    }
    return null;
  }

  private Resolver renameOperation(Resolver original, Type repositoryResourceType) {
    String name = original.getOperationName(), typeName = ((Class<?>) repositoryResourceType).getSimpleName();
    if (name.startsWith("exists")) {
      name = name.replace("exists", typeName + "Exists");
    } else if (name.contains("All")) {
      name = name.replace("All", "All" + typeName + "s");
    } else if (name.startsWith("find")) {
      name = name.replace("find", "find" + typeName);
    } else if (name.startsWith("delete")) {
      name = name.replace("delete", "delete" + typeName);
    } else if (name.startsWith("update")) {
      name = name.replace("update", "update" + typeName);
    } else if (name.equals("count")) {
      name = name.replace("count", "count" + typeName + "s");
    } else {
      name += typeName;
    }
    if (isSorted(original.getArguments())) {
      name += "Sorted";
    }
    return new Resolver(name, original.getOperationDescription(), original.getOperationDeprecationReason(),
      original.isBatched(), original.getExecutable(), original.getTypedElement(), original.getArguments(),
      original.getComplexityExpression());
  }

  private boolean isSorted(List<OperationArgument> arguments) {
    return arguments.stream().filter(a -> Sort.class.isAssignableFrom(a.getParameter().getType())).count() > 0;
  }

}
