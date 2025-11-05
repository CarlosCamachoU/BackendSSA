package com.example.BackendSSA.Security;

import java.util.Date;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;


@Component
@CrossOrigin

public class JwtGenerador {
    
    public String generarToken(Authentication authentication){
        String email = authentication.getName();
        Date tiempoActual = new Date();
        Date expiracionToken = new Date(tiempoActual.getTime() + ConstantesSeguridad.JWT_EXPIRATION_TOKEN);


        String token = Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(expiracionToken)
            .signWith(SignatureAlgorithm.HS512, ConstantesSeguridad.JWT_FIRMA)
            .compact();
        return token;
    }

    public String obtenerUsernameDeJwt(String token){
        Claims claims = Jwts.parser()
            .setSigningKey(ConstantesSeguridad.JWT_FIRMA)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();

    }

    public Boolean ValidarToken(String token){
        try{
            Jwts.parser().setSigningKey(ConstantesSeguridad.JWT_FIRMA).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
           throw new AuthenticationCredentialsNotFoundException("El token no es valido, puede haber expirado");
        }
    }
}
