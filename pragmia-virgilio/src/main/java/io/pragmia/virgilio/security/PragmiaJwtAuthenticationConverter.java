package io.pragmia.virgilio.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.*;

public class PragmiaJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtGrantedAuthoritiesConverter conv = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, conv.convert(jwt), jwt.getSubject());
    }
}
