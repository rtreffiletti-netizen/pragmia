#!/bin/bash
# Genera il keystore JKS per PRAGMIA-SAML (sviluppo/test)
# In produzione: usa un certificato firmato da CA

KEYSTORE_DIR="../src/main/resources/saml"
mkdir -p "$KEYSTORE_DIR"

keytool -genkeypair \
  -alias pragmia-saml \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -validity 3650 \
  -keystore "$KEYSTORE_DIR/pragmia-saml.jks" \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=PRAGMIA SAML, OU=IAM, O=Pragmia, L=Roma, ST=Lazio, C=IT"

echo "[OK] Keystore generato in $KEYSTORE_DIR/pragmia-saml.jks"

# Esporta anche il certificato pubblico (da condividere con gli SP)
keytool -export \
  -alias pragmia-saml \
  -keystore "$KEYSTORE_DIR/pragmia-saml.jks" \
  -storepass changeit \
  -file "$KEYSTORE_DIR/pragmia-saml.crt" \
  -rfc

echo "[OK] Certificato pubblico esportato in $KEYSTORE_DIR/pragmia-saml.crt"
