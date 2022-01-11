package tk.andrei.medicalapp.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
public class JwtServiceImplementation implements JwtService {
    private final String issuer = "Andrei";

    @Value("${jwt.secret}")
    private String secret;

    private Algorithm algorithm;

    public Algorithm getAlgorithm() {
        if (this.algorithm == null) {
            this.algorithm = Algorithm.HMAC256(this.secret.getBytes());
        }
        return this.algorithm;
    }

    @Override
    public String createAccessToken(String username, List authorities) {
        // two days
        int minutes = 2880;
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + minutes * 60 * 1000))
                .withIssuer(this.issuer)
                .withClaim("roles", authorities)
                .sign(this.getAlgorithm());
    }

    @Override
    public String createRefreshToken(String username) {
        // 20 days
        int minutes = 28800;
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + minutes * 60 * 1000))
                .withIssuer(this.issuer)
                .sign(this.getAlgorithm());
    }

    @Override
    public DecodedJWT decodeToken(String token) {
        JWTVerifier jwtVerifier = JWT.require(this.getAlgorithm()).build();
        return jwtVerifier.verify(token);
    }

    @Override
    public String getUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities(DecodedJWT decodedJWT) {
        String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        stream(roles).forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role));
        });

        return authorities;
    }

    @Override
    public Map<String, String> setTokens(String accessToken, String refreshToken){
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        return tokens;
    }
}
