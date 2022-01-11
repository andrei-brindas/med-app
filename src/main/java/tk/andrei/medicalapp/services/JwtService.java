package tk.andrei.medicalapp.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface JwtService {
    String createAccessToken(String username, List authorities);
    DecodedJWT decodeToken(String token);
    String getUsername(DecodedJWT decodedJWT);
    Collection<SimpleGrantedAuthority> getAuthorities(DecodedJWT decodedJWT);
    String createRefreshToken(String username);
    Map<String, String> setTokens(java.lang.String accessToken, java.lang.String refreshToken);
}
