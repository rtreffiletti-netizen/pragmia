# PRAGMIA SAML — Modulo SAML2 IdP + SP

Modulo SAML2 production-ready per PRAGMIA IAM Platform.

## Modalità operative

### IdP (Identity Provider)
`GET /saml/idp/metadata` — Metadata XML/JSON
`POST /saml/idp/sso` — SSO HTTP-POST
`POST /saml/idp/slo` — Single Logout

### SP (Service Provider)
`GET /saml/sp/metadata` — Metadata XML
`GET /saml/sp/acs` — Assertion Consumer Service
`GET /saml/sp/slo` — Logout

## Abilitazione
```yaml
pragmia:
  modules:
    samlEnabled: true
  saml:
    idp:
      enabled: true
      entityId: https://pragmia.io/saml/idp
    sps:
      - entityId: https://external-idp.com/saml/sp
        jitProvisioning: true
```

## Binding
- HTTP-POST (SSO + SLO)
- HTTP-Redirect/GET (SSO)

## Note
I certificati X.509 hanno sede in `src/main/resources/certs/`.
