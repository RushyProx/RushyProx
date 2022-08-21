package net.rushnation.rushyprox.network;

import com.nukkitx.protocol.bedrock.BedrockPong;
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.event.list.ProxyPingEvent;
import net.rushnation.rushyprox.network.upstream.FirstUpstreamHandler;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class ProxyListener implements BedrockServerEventHandler {

    private static final BedrockPong PONG = new BedrockPong();

    private final ProxyServer proxyServer;

    static {
        PONG.setVersion(ProxyServer.MINECRAFT_VERSION);
        PONG.setProtocolVersion(ProxyServer.PROTOCOL_VERSION);
        PONG.setIpv4Port(ProxyServer.RUNNING_PORT);
        PONG.setIpv6Port(ProxyServer.RUNNING_PORT);
        PONG.setGameType("Survival");
        PONG.setMotd("RushProx Alpha Version");
        PONG.setSubMotd("RushProx");
        PONG.setMaximumPlayerCount(10);
        PONG.setPlayerCount(0);
        PONG.setEdition("MCPE");
    }

    @Override
    public boolean onConnectionRequest(InetSocketAddress address) {
        // Check if there a enough slots remaining
        return true;
    }

    @Override
    public BedrockPong onQuery(InetSocketAddress inetSocketAddress) {

        // Proxy has been pinged
        this.proxyServer.getEventManager().call(new ProxyPingEvent(inetSocketAddress));

        return PONG;
    }

    @Override
    public void onSessionCreation(BedrockServerSession bedrockServerSession) {
        bedrockServerSession.setPacketHandler(new FirstUpstreamHandler(bedrockServerSession, this.proxyServer));
    }

    @Override
    public void onUnhandledDatagram(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf packetContent = packet.content();
    }
}
