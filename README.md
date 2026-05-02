# PRAGMIA — Policy Rights Access Governance Management Intelligence Architecture
> Perché ogni accesso racconta una storia.

## Quick Start
```bash
docker-compose up -d
mvn clean package -DskipTests
java -jar pragmia-distribution/target/pragmia.jar
```

| Endpoint            | URL                                          |
|---------------------|----------------------------------------------|
| Swagger UI          | http://localhost:8080/swagger-ui.html        |
| OIDC Discovery      | http://localhost:8080/.well-known/openid-configuration |
| JWKS                | http://localhost:8080/oauth2/jwks            |
| Actuator            | http://localhost:8080/actuator/health        |

| Modulo       | Descrizione                                  | Tier        |
|--------------|----------------------------------------------|-------------|
| VIRGILIO     | Auth Engine OAuth 2.1/OIDC + Flow Engine     | Community   |
| CANTO        | Authentication Flow Trees CRUD               | Community   |
| SOGLIA       | Gateway / Policy Enforcement Point           | Community   |
| CLIO         | Audit Log immutabile PostgreSQL              | Community   |
| BEATRICE     | NLP Admin Ollama + dual-approval             | Enterprise  |
| MINOS        | Policy Engine ABAC SpEL                      | Enterprise  |
| LUCE         | Compliance Pack NIS2/DORA/GDPR/AgID          | Enterprise  |
| SIBILLA      | Provisioning SCIM 2.0 / LDAP sync            | Enterprise  |
