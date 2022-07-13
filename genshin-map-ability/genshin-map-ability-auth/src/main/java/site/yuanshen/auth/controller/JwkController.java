package site.yuanshen.auth.controller;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/**
 * Jwk密钥交换用Controller
 *
 * @author Moment
 */
@RestController
@RequestMapping
public class JwkController {

    private final KeyPair keyPair;

    @Autowired
    public JwkController(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwk() {
        return new JWKSet(
                new RSAKey.Builder(
                        (RSAPublicKey) keyPair.getPublic()
                ).build()
        ).toJSONObject();
    }
}
