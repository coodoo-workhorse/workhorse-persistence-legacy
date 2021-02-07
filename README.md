[logo]: https://gitlab.coodoo.io/workhorse/workhorse-persistence-mysql-legacy/-/raw/master/logo.png "Workhorse: Extendable Java Job Engine for background jobs and business critical tasks"

# Workhorse Persistence MySQL Legacy ![alt text][logo]

> Legacy support for the MySQL Persistence of Workhorse version 1.5

## Table of Contents
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

Create the WAR file

```
mvn clean package
```

It will appear in `/workhorse-persistence-mysql-legacy/target/workhorse-persistence-mysql-legacy.war`


## Getting started

Drop the WAR into your autodeploy folder `/wildfly/standalone/deployments/`


## Changelog

All release changes can be viewed on our [changelog](./CHANGELOG.md).


## Maintainers

[coodoo](https://github.com/orgs/coodoo-io/people)


## Contribute

Pull requests and issues are welcome.


## License

[Apache-2.0 © coodoo GmbH](./LICENSE)

Logo: [Martin Bérubé](http://www.how-to-draw-funny-cartoons.com)
  