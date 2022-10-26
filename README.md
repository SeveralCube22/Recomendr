# Setup

To setup the following project, the following are required:

- Java
- Maven
- NPM

Additionally, an AWS Keyspaces keyspace and an AWS MSK cluster are required for both the backend and storm modules:

- AWS Keyspace
  - An application.conf is needed 
    - The credentials for the AWS user
    - A truststore.jks
    - Replace the application.conf in both backend and storm modules

- AWS MSKS
  - The AWS Key Id and AWS Secret Key need to be provided as Java system properties for both backend and storm modules
  

# Overview

This project is a naive implementation of a real-time recommendation system proposed by Tencent. [Tencent Rec Paper](https://dl.acm.org/doi/10.1145/2723372.2742785)

The backend module contains the endpoints whereas the storm module includes the definition for the topology to process real-time user events. The frontend is not yet completed. 

| Endpoint  | HTTP Methods | Request Body | RequestParameter | Description
| ------------- | ------------- | ------------- | ------------- | ------------- |
| /api/publish/movie  | POST | MovieDTO | | Stores a movie
| /api/publish/userevent  | POST  | UserEvent | | Produces a user event onto Kafka topic
| /api/recommendations | GET | | userId | Retrieves a list of unseen movie recommendations sorted by highest predicated rating

