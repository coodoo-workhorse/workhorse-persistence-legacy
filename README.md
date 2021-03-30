# Persistence for Workhorse 1.5 Legacy

> Legacy persistence to support Workhorse runnning in version 1.5

## Table of Contents
<img align="right" height="200px" src="logo.png">

- [Prerequisites](#prerequisites)
- [Install](#install)
- [Getting started](#getting-started)
- [Maintainers](#maintainers)
- [Changelog](#changelog)
- [Contribute](#contribute)
- [License](#license)
  

## Prerequisites

Before you begin, ensure you have met the following requirements:
* You have installed at least [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* You have [Maven](https://maven.apache.org/download.cgi) running on your system
 

## Install

1. Add the following dependency to your project ([published on Maven Central](https://search.maven.org/artifact/io.coodoo/workhorse-persistence-legacy/))
   
   ```xml
   <dependency>
       <groupId>io.coodoo</groupId>
       <artifactId>workhorse-persistence-legacy</artifactId>
       <version>2.0.0-RC1-SNAPSHOT</version>
   </dependency>
   ```
   
2. Create the database tables and add the JPA entities to your persistence.xml
   
   You can find a SQL script to create the tables [here](./src/main/resources/mysql-schema.sql).
   
   ```xml
	<class>io.coodoo.workhorse.persistence.legacy.entity.LegacyJob</class>
	<class>io.coodoo.workhorse.persistence.legacy.entity.LegacyExecution</class>
	<class>io.coodoo.workhorse.persistence.legacy.entity.LegacyExecutionView</class>
	<class>io.coodoo.workhorse.persistence.legacy.entity.LegacyConfig</class>
	<class>io.coodoo.workhorse.persistence.legacy.entity.LegacyLog</class>
   ```
3. To provide the EntityManager you have to implement a `@JobEngineEntityManagerProducer` CDI producer.

   ```java
    @Stateless
    public class JobEngineEntityManagerProducer {
    
        @PersistenceContext
        private EntityManager entityManager;
    
        @Produces
        @JobEngineEntityManager
        public EntityManager getEntityManager() {
            return entityManager;
        }
    }
    ```
    *This is necessary to avoid trouble when it comes to different persistence contexts.*



## Getting started

After the [installation](#install) all you need is to create an `LegacyConfig` instance an pass it to the `start()` method of the `WorkhorseService`.

```java
@Inject
WorkhorseService workhorseService;

public void startWithLegacyPersistence() {
    LegacyConfig legacyConfig = new LegacyConfigBuilder().build();
    workhorseService.start(legacyConfig);
}
```

If you are using this persistence, you should already have your configuration provided in your database. Otherwise you can pass the configuration you want using the builder on `LegacyConfig`.



## MySQL database via Docker

You can spawn a MySQL 8 Server with the [legacy data structure](./src/main/resources/mysql-schema.sql) using Docker:

```bash
docker-compose up db
```

The schema name is `workhorse`, so is the username and password. You can see and change these settings in [docker-compose.yml](./docker-compose.yml).



## Changelog

All release changes can be viewed on our [changelog](./CHANGELOG.md).


## Maintainers

[coodoo](https://github.com/orgs/coodoo-io/people)


## Contribute

Pull requests and issues are welcome.


## License

[Apache-2.0 © coodoo GmbH](./LICENSE)

Logo: [Martin Bérubé](http://www.how-to-draw-funny-cartoons.com)
  
