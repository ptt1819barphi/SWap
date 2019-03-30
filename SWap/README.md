# Build Instructions
## Requirements
* Java
* Gradle
* CoreNLP Server

## Usage
There are 2 different ways to start this application:

### Gradle
```bash
$ gradle run --args=<path to config>
```

### Plain Java
```bash
$ gradle shadowJar
$ java -jar SWap-all.jar <path to config>
```

## Config
An example config is provided: sample-config.json