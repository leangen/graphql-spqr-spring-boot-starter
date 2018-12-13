# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [0.0.3] - 2018-11-26
### Added
- Full support for Apollo's graphql-ws protocol [#13](https://github.com/leangen/graphql-spqr-spring-boot-starter/issues/13)
- Easy way to inject custom global context [#11](https://github.com/leangen/graphql-spqr-spring-boot-starter/issues/11)
- Support for Reactor types (`Flux` and `Mono`) [#16](https://github.com/leangen/graphql-spqr-spring-boot-starter/issues/16)

### Changed
- [Breaking] Normalized all application properties. See [SpqrProperties](https://github.com/leangen/graphql-spqr-spring-boot-starter/blob/graphql-spqr-spring-boot-starter-v0.0.3/graphql-spqr-spring-boot-autoconfigure/src/main/java/io/leangen/graphql/spqr/spring/autoconfigure/SpqrProperties.java) for details.
- [Breaking] Default GUI endpoint changed from `/graphiql` to `/gui`
- Upgraded to [graphql-spqr 0.9.9](https://github.com/leangen/graphql-spqr/releases/tag/graphql-spqr-v0.9.9)
- GraphiQL replaced with [GraphQL Playground](https://github.com/prisma/graphql-playground) (might be revised later)

### Removed
- [Breaking] Removed `DefaultGlobalContext#getDataLoaders` as `DataLoader`s are now [accessible directly](https://github.com/graphql-java/graphql-java/pull/1263) 

### Fixed
- Proxied beans no longer cause an exception (enabling the usage of Spring Security, `@Transactional` etc) [#12](https://github.com/leangen/graphql-spqr-spring-boot-starter/issues/12)
