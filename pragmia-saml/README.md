# PRAGMIA-SAML — SAML2 IdP + SP Engine

Modulo PRAGMIA per il supporto completo del protocollo SAML 2.0.
Implementa sia la funzione di **Identity Provider** (emissione assertion)
che di **Service Provider** (consumo assertion da IdP esterni).

## Funzionalità

### IdP Mode (PRAGMIA emette assertion SAML)
- SSO POST e Redirect Binding
- SP-Initiated + IdP-Initiated flow
- Metadata endpoint (`/saml/idp/metadata`)
- Firma assertion con chiave RSA (keystore JKS/PKCS12)
- Single Logout (SLO)
- Session management con scadenza configurabile

### SP Mode (PRAGMIA consuma assertion da IdP esterni)
- Azure Active Directory / ADFS
- SPID (AgID — Pubblica Amministrazione italiana)
- CIE (Carta d'Identità Elettronica)
- Qualsiasi IdP SAML2-compliant
- Metadata SP per ogni registrationId (`/saml/sp/{id}/metadata`)

### Admin API
- Registrazione/gestione SP dinamica (`/api/admin/v1/saml/service-providers`)
- Session management SAML (`/api/admin/v1/saml/sessions`)
- Audit log SAML append-only (`/api/admin/v1/saml/audit`)

## Configurazione rapida

```yaml
pragmia:
  modules:
    saml:
      enabled: true
  saml:
    idp:
      entity-id: "https://auth.yourcompany.com/saml/idp"
      keystore-path: "classpath:saml/pragmia-saml.jks"
      keystore-password: "${PRAGMIA_SAML_KEYSTORE_PASSWORD}"
      key-alias: "pragmia-saml"
```

## Generare il keystore JKS (sviluppo/test)

```bash
keytool -genkeypair \
  -alias pragmia-saml \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -validity 3650 \
  -keystore src/main/resources/saml/pragmia-saml.jks \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=PRAGMIA SAML, OU=IAM, O=Pragmia, L=Roma, ST=Lazio, C=IT"
```

## Endpoint principali

| Endpoint | Descrizione |
|---|---|
| `GET /saml/idp/metadata` | Metadata IdP (XML) |
| `GET/POST /saml/idp/sso` | SSO endpoint (SP-Initiated) |
| `POST /saml/idp/slo` | Single Logout endpoint |
| `POST /api/saml/idp/initiate` | IdP-Initiated SSO |
| `GET /saml/sp/{id}/metadata` | Metadata SP per IdP esterno |
| `GET /saml/sp/post-login` | Landing page post-login SP |
| `GET /FederationMetadata/2007-06/FederationMetadata.xml` | Compatibilità ADFS |

## Database

Il modulo gestisce il proprio schema con prefisso `pragmia_saml_*`.
Le migration Flyway sono in `src/main/resources/db/migration/saml/`.

---
*PRAGMIA-SAML v1.0.0 — © 2026 Roberto Treffiletti*
