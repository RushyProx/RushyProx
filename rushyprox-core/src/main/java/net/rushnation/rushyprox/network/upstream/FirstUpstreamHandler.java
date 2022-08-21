package net.rushnation.rushyprox.network.upstream;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.Bootstrap;
import net.rushnation.rushyprox.console.Console;
import net.rushnation.rushyprox.network.LoginEncryption;
import net.rushnation.rushyprox.player.ProxyPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@RequiredArgsConstructor
@Log4j2
public class FirstUpstreamHandler implements BedrockPacketHandler {

    private final BedrockServerSession serverSession;
    private final ProxyServer proxyServer;

    @Override
    public boolean handle(LoginPacket loginPacket) {
        int protocolVersion = loginPacket.getProtocolVersion();

        ProxyServer.getProxyServer().getLogger().info("Debug Login Packet");

        if (protocolVersion != ProxyServer.PROTOCOL_VERSION) {
            PlayStatusPacket playStatusPacket = new PlayStatusPacket();

            playStatusPacket.setStatus(protocolVersion > ProxyServer.PROTOCOL_VERSION ? PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD : PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
            this.serverSession.sendPacketImmediately(playStatusPacket);
            this.serverSession.disconnect();
        }

        LoginEncryption loginEncryption = new LoginEncryption(this.serverSession);

        loginEncryption.encryptPlayerConnection(loginPacket).whenComplete(((playerData, throwable) ->
        {
            this.serverSession.setPacketCodec(ProxyServer.BEDROCK_PACKET_CODEC); //set PacketCode


            // Create Player here.
            ProxyPlayer player = new ProxyPlayer(this.proxyServer, loginEncryption.getPacket(), this.serverSession, playerData, loginEncryption.getKeyPair());

            Console.proxyPlayer = player;

            InetSocketAddress address = null;
            try {
                address = new InetSocketAddress(InetAddress.getByName("ali.rushnation.net"), 19132);
            } catch (UnknownHostException e) {
                ProxyServer.getProxyServer().getLogger().error("{}", e, e);
            }

            player.connect(address);  // Connecting player
        }));

        return true;
    }


}
