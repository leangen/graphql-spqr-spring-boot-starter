# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- [Breaking] Normalized all application properties. See [SpqrProperties]() for details
- [Breaking] Default GUI endpoint changed from /graphiql to /gui
- Upgraded to [graphql-spqr 0.9.9](https://github.com/leangen/graphql-spqr/releases/tag/graphql-spqr-v0.9.9)
- GraphiQL replaced with [GraphQL Playground](https://github.com/prisma/graphql-playground)

### Removed
- [Breaking] Removed `DefaultGlobalContext#getDataLoaders` as `DataLoader`s are now [accessible directly](https://github.com/graphql-java/graphql-java/pull/1263) 

### Fixed
- Proxied beans no longer cause an exception (enabling the usage of Spring Security, `@Transactional` etc) [#12](https://github.com/leangen/graphql-spqr-spring-boot-starter/issues/12)

### Added
- Full support for Apollo 
