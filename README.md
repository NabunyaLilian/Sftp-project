# sftp-project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Description

This project implements secure file transfer functionality using **SFTP (Secure File Transfer Protocol)** to facilitate reliable, encrypted transfer of files between remote servers or between a local machine and a remote server.

Key features include:

* Establishing SSH connections to remote hosts for secure authentication and communication.
* Uploading and downloading files and directories with support for recursive operations.
* Handling file permissions and transfer errors robustly to ensure data integrity.
* Designed with security best practices, including encrypted channels and credential management.

This project is ideal for environments requiring secure data exchange across network boundaries, such as cloud deployments, backup solutions, and remote system administration.

---

Would you like me to tailor it more towards a specific use case or add technical stack details (like Java, Python, or Quarkus if you used them)?


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Swagger UI
You can access the swagger documentation via
[http://localhost:8080/q/swagger-ui/](http://localhost:8080/q/swagger-ui/ )

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/sftp-project-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- JSch ([guide](https://quarkiverse.github.io/quarkiverse-docs/quarkus-jsch/dev/index.html)): Secure your connections with SSH
- RESTEasy Classic ([guide](https://quarkus.io/guides/resteasy)): REST endpoint framework implementing Jakarta REST and more
- Logging JSON ([guide](https://quarkus.io/guides/logging#json-logging)): Add JSON formatter for console logging

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
