package net.rushnation.rushyprox.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import lombok.extern.log4j.Log4j2;

import java.net.URI;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ForgeryUtils {

    //CREDITS : https://github.com/CloudburstMC/Plexus/

    public static SignedJWT forgeAuthData(KeyPair pair, JSONObject extraData) {
        String publicKeyBase64 = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        URI x5u = URI.create(publicKeyBase64);

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES384).x509CertURL(x5u).build();

        long timestamp = System.currentTimeMillis();
        Date nbf = new Date(timestamp - TimeUnit.SECONDS.toMillis(1));
        Date exp = new Date(timestamp + TimeUnit.DAYS.toMillis(1));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .notBeforeTime(nbf)
                .expirationTime(exp)
                .issueTime(exp)
                .issuer("self")
                .claim("certificateAuthority", true)
                .claim("extraData", extraData)
                .claim("identityPublicKey", publicKeyBase64)
                .build();

        SignedJWT jwt = new SignedJWT(header, claimsSet);

        try {
            EncryptionUtils.signJwt(jwt, (ECPrivateKey) pair.getPrivate());
        } catch (JOSEException e) {
            log.error("{}", e, e);
        }

        return jwt;
    }

    public static JWSObject forgeSkinData(KeyPair pair, JSONObject skinData) {
        URI x5u = URI.create(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES384).x509CertURL(x5u).build();

        JWSObject jws = new JWSObject(header, new Payload(skinData));

        try {
            EncryptionUtils.signJwt(jws, (ECPrivateKey) pair.getPrivate());
        } catch (JOSEException e) {
            log.error("{}", e, e);
        }

        return jws;
    }
}
