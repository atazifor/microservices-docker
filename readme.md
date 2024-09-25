# Deploying Microservices Using Docker

Here we want to run fully automated tests of the microservice landscape that start all microservices as Docker containers.
No other infrastructure needed except for Docker engine.
- With docker we can verify that microservices work on our local dev computers
- Same tests can be automatically run on a build server to ensure that our changes didn't break tests at the system level
- We don't need a dedicated environment to run these types of tests.

## What is Docker
Docker is a lightweight alternative to Virtual Machines (VM). Unlike VMs that use a hypervisor to run a copy of an operating system in each VM,
docker containers are just namespaces (containers) in Linux with control groups that are used to limit the amount of CPU and memory each container is allowed to use.
Hence docker containers use the Host OS, while each VM has it's own copy of an OS, on top of the Host OS.

## Java with Docker
Historically CPU amd Memory limits set for Docker containers using **linux cgroups** have not been respected by Java; java simply ignored these settings.
Instead of Java allocating memory inside the JVM with what was set for the container, it allocates the entire memory of the Host OS. 

Java SE 9 provided initial support for container-based CPU and memory constraints. This was improved in Java SE 10.

### Java 17 with CPU and Memory Limits on Docker Engine
When you start docker with a allocated memory, M, the default max heap size allocated is 1/4 of the total memory. 
```shell
(base) ➜  microservices-docker git:(main) ✗ docker run -it --rm -m=1024M eclipse-temurin:17 java -XX:+PrintFlagsFinal | grep 'size_t MaxHeapSize'
   size_t MaxHeapSize                              = 268435456                                 {product} {ergonomic}
```
Allocating 1G (1024MB) to docker, will result in 256MB of heap (268435456/20124/1024 ~ 256MB)

If is also possible to specify the max heap with the following command that allocates a max heap size of 600MB:
```shell
(base) ➜  microservices-docker git:(main) ✗ docker run -it --rm -m=1024M eclipse-temurin:17 java -Xmx600m -XX:+PrintFlagsFinal | grep 'size_t MaxHeapSize'
   size_t MaxHeapSize                              = 629145600                                 {product} {command line}
```
If we try to allocate a byte array whose size is greater than the heap size of 256MB we get a `java.lang.OutOfMerroryError`:
```shell
(base) ➜  microservices-docker git:(main) ✗ echo 'new byte[500_000_000]' | docker run -i --rm -m=1024m eclipse-temurin:17 jshell -q
Sep 24, 2024 11:04:53 AM java.util.prefs.FileSystemPreferences$1 run
INFO: Created user preferences directory.
jshell> new byte[500_000_000]|  Exception java.lang.OutOfMemoryError: Java heap space
|        at (#1:1)
jshell> %  
```

## Package Microservice in Docker Container
To use Docker with a microservice we need to package the microservice into a docker image. And in order to achieve this we need a **docker file**.
Since each microservice will run in it's own docker container, which is isolated from other containers [has its own IP address, hostname and ports],
we can use a default port of 8080 in each container.

So running the microservice locally is different from when running using docker. To achieve diffent configurations for each way of running the 
microservice, we use **Spring Profiles**.