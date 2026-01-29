# sample.daytrader7 [![Build Status](https://travis-ci.org/WASdev/sample.daytrader7.svg?branch=master)](https://travis-ci.org/WASdev/sample.daytrader7)

# DayTrader: Cloud-Native Stock Trading Platform

## Overview

DayTrader is a comprehensive benchmark and performance sample application built around the paradigm of an online stock trading system. The application allows users to login, view their portfolio, lookup stock quotes, and buy or sell stock shares. With the aid of a Web-based load driver such as Apache JMeter, the real-world workload provided by DayTrader can be used to measure and compare the performance of Java Platform, Enterprise Edition (Java EE) application servers offered by a variety of vendors. In addition to the full workload, the application also contains a set of primitives used for functional and performance testing of various Java EE components and common design patterns.

DayTrader is an end-to-end benchmark and performance sample application. It provides a real world Java EE workload. DayTrader's original design spans Java EE 7, including the new WebSockets specification. Other Java EE features include JSPs, Servlets, EJBs, JPA, JDBC, JSF, CDI, Bean Validation, JSON, JMS, MDBs, and transactions (synchronous and asynchronous/2-phase commit).

## Modernization Initiative

DayTrader has been modernized from a traditional Java EE7 application running on WebSphere Liberty to a cloud-native architecture with a Quarkus backend and modern React SPA frontend. This modernization delivers significant improvements including faster startup times (sub-second cold starts), reduced memory footprint, containerized deployment, and a contemporary user experience with a responsive Material UI-based interface.

## Project Structure

This repository contains multiple versions of the DayTrader application:

| Module | Description | Documentation |
|--------|-------------|-----------------|
| **daytrader-ee7/** | Original Java EE7 application (legacy) | [README](daytrader-ee7/README.md) |
| **daytrader-quarkus/** | Modernized Quarkus backend with REST APIs | [README](daytrader-quarkus/README.md) |
| **daytrader-frontend/** | Modern React + TypeScript SPA frontend | [README](daytrader-frontend/README.md) |

## Architecture Specifications

Detailed architecture documentation and migration specifications are available in the `specs/` folder:

- [Executive Summary](specs/00-executive-summary.md) - High-level overview of the modernization initiative
- [Current Architecture](specs/01-current-architecture.md) - Legacy Java EE7 architecture
- [Target Architecture](specs/02-target-architecture.md) - Cloud-native Quarkus + React architecture
- [Backend Migration Spec](specs/03-backend-migration-spec.md) - Quarkus backend implementation details
- [Frontend Migration Spec](specs/04-frontend-migration-spec.md) - React frontend implementation details

## Quick Start

### Running the Legacy Java EE7 Version

To run the original Java EE7 application on Open Liberty:

```bash
# Clone the repository
git clone git@github.com:WASdev/sample.daytrader7.git
cd sample.daytrader7

# Build and start the application
mvn install
cd daytrader-ee7
mvn liberty:run
```

Once the server has been started, go to [http://localhost:9082/daytrader](http://localhost:9082/daytrader) to interact with the sample.

### Running the Modernized Quarkus + React Version

To run the cloud-native version with Quarkus backend and React frontend:

```bash
# Clone the repository
git clone git@github.com:WASdev/sample.daytrader7.git
cd sample.daytrader7

# Start the Quarkus backend (in one terminal)
cd daytrader-quarkus
./mvnw quarkus:dev

# Start the React frontend (in another terminal)
cd daytrader-frontend
npm install
npm run dev
```

The application will be available at [http://localhost:5173](http://localhost:5173) (frontend) with the backend API running at [http://localhost:8080](http://localhost:8080).

For detailed setup and development instructions, see:
- [Quarkus Backend README](daytrader-quarkus/README.md)
- [React Frontend README](daytrader-frontend/README.md)

## Containerization

### Legacy Java EE7 with DB2

To containerize the original application with DB2:

```bash
docker build -t sample-daytrader7 -f Containerfile_db2 .
```

### Modernized Version

For containerization of the Quarkus + React version, refer to the respective module READMEs:
- [Quarkus Backend Containerization](daytrader-quarkus/README.md)
- [React Frontend Containerization](daytrader-frontend/README.md)

## Notice

© Copyright IBM Corporation 2015.

## License

```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````
