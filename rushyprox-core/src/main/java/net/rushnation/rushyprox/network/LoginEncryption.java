package net.rushnation.rushyprox.network;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.network.util.Preconditions;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import io.netty.util.AsciiString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.player.data.PlayerData;
import net.rushnation.rushyprox.util.ForgeryUtils;

import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class LoginEncryption {

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final BedrockServerSession serverSession;

    @Getter
    private LoginPacket packet;

    @Getter
    private KeyPair keyPair;

    private static boolean validateChain(JsonNode data) throws Exception {
        ECPublicKey lastKey = null;
        boolean validChain = false;
        for (JsonNode node : data) {
            JWSObject jwt = JWSObject.parse(node.asText());

            if (!validChain) {
                validChain = EncryptionUtils.verifyJwt(jwt, EncryptionUtils.getMojangPublicKey());
            }

            if (lastKey != null) {
                EncryptionUtils.verifyJwt(jwt, lastKey);
            }

            JsonNode payloadNode = JSON_MAPPER.readTree(jwt.getPayload().toString());
            JsonNode ipkNode = payloadNode.get("identityPublicKey");
            Preconditions.checkState(ipkNode != null && ipkNode.getNodeType() == JsonNodeType.STRING, "identityPublicKey node is mivssing in chain");
            lastKey = EncryptionUtils.generateKey(ipkNode.asText());
        }
        return validChain;
    }

    private void verifyJwt(JWSObject jwt, ECPublicKey key) throws JOSEException {
        jwt.verify(new DefaultJWSVerifierFactory().createJWSVerifier(jwt.getHeader(), key));
    }

    @SneakyThrows
    public CompletableFuture<PlayerData> encryptPlayerConnection(LoginPacket loginPacket) {
        JsonNode certData = JSON_MAPPER.readTree(loginPacket.getChainData().toByteArray());

        JsonNode certChainData = certData.get("chain");
        if (certChainData.getNodeType() != JsonNodeType.ARRAY) {
            RuntimeException runtimeException = new RuntimeException("Certificate data is not valid");
            ProxyServer.getProxyServer().getLogger().error("{}", runtimeException, runtimeException);
        }

        ArrayNode chainData = (ArrayNode) certChainData;
        try {
            if (!validateChain(certChainData)) {
                this.serverSession.disconnect("You are not Authenticated");
            }
            JWSObject jwt = JWSObject.parse(certChainData.get(certChainData.size() - 1).asText());
            JsonNode payload = JSON_MAPPER.readTree(jwt.getPayload().toBytes());

            if (payload.get("extraData").getNodeType() != JsonNodeType.OBJECT) {
                RuntimeException runtimeException = new RuntimeException("AuthData was not found!");
                ProxyServer.getProxyServer().getLogger().error("{}", runtimeException, runtimeException);
            }

            JSONObject extraData = (JSONObject) jwt.getPayload().toJSONObject().get("extraData");

            ECPublicKey identityPublicKey = EncryptionUtils.generateKey(payload.get("identityPublicKey").textValue());

            JWSObject clientJwt = JWSObject.parse(loginPacket.getSkinData().toString());
            JSONObject skinData = (JSONObject) clientJwt.getPayload().toJSONObject();

            this.verifyJwt(clientJwt, identityPublicKey);
            this.createKeyPair(certChainData, chainData, extraData, skinData);

            return CompletableFuture.completedFuture(this.encryptPlayerData(extraData));
        } catch (Exception e) {
            this.serverSession.disconnect("failed to login");
            ProxyServer.getProxyServer().getLogger().error("{}", e, e);
            return null;
        }
    }

    @SneakyThrows
    public void createKeyPair(JsonNode certChainData, ArrayNode chainData, JSONObject extraData, JSONObject skinData) {
        this.keyPair = EncryptionUtils.createKeyPair();

        SignedJWT signedExtraData = ForgeryUtils.forgeAuthData(keyPair, extraData);
        JWSObject signedSkinData = ForgeryUtils.forgeSkinData(keyPair, skinData);

        chainData.remove(certChainData.size() - 1);

        chainData.add(signedExtraData.serialize());

        JsonNode json = JSON_MAPPER.createObjectNode().set("chain", chainData);

        AsciiString loginChainData = new AsciiString(JSON_MAPPER.writeValueAsBytes(json));

        this.createLoginPacket(loginChainData, signedSkinData);
    }

    public void createLoginPacket(AsciiString loginChainData, JWSObject signedSkinData) {
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.setChainData(loginChainData);
        loginPacket.setSkinData(AsciiString.of(signedSkinData.serialize()));
        loginPacket.setProtocolVersion(ProxyServer.PROTOCOL_VERSION);

        this.packet = loginPacket;
    }

    public PlayerData encryptPlayerData(JSONObject data) {
        return PlayerData.builder()
                .uuid(UUID.fromString(data.getAsString("identity")))
                .XuId(data.getAsString("XUID"))
                .name(data.getAsString("displayName"))
                .build();
    }
}
