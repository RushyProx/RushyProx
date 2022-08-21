package net.rushnation.rushyprox.network.downstream;

import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.protocol.PacketHandler;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.player.ProxyPlayer;

import javax.crypto.SecretKey;
import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

@RequiredArgsConstructor
public class FirstDownstreamHandler implements BedrockPacketHandler {

    protected final ProxyPlayer player;

    @Override
    public boolean handle(ServerToClientHandshakePacket packet) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(packet.getJwt());
            URI uri = signedJWT.getHeader().getJWKURL();
            ECPublicKey key = EncryptionUtils.generateKey(uri.toASCIIString());
            SecretKey secretKey = EncryptionUtils.getSecretKey(this.player.getProxyKey().getPrivate(), key, Base64.getDecoder().decode(signedJWT.getJWTClaimsSet().getStringClaim("salt")));

            player.getDownstream().enableEncryption(secretKey);
        } catch (Exception e) {
            ProxyServer.getProxyServer().getLogger().error("{}", e, e);
        }
        return true;
    }
}
