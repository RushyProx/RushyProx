package net.rushnation.rushyprox.player;

import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockClientSession;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;
import com.nukkitx.protocol.bedrock.packet.TextPacket;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.command.type.PlayerCommandSender;
import net.rushnation.rushyprox.network.ProxyBatchHandler;
import net.rushnation.rushyprox.network.downstream.ConnectedDownstreamHandler;
import net.rushnation.rushyprox.network.downstream.FirstDownstreamHandler;
import net.rushnation.rushyprox.network.downstream.SwitchDownstreamHandler;
import net.rushnation.rushyprox.network.upstream.ConnectedUpstreamHandler;
import net.rushnation.rushyprox.player.data.PlayerData;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.UUID;

@RequiredArgsConstructor
@Data
public class ProxyPlayer implements IProxyPlayer, PlayerCommandSender {

    private final ProxyServer proxyServer;
    private final LoginPacket loginPacket;
    private final BedrockServerSession upstream;
    private final PlayerData playerData;
    private final KeyPair proxyKey;

    private BedrockClient client;
    private BedrockClientSession connectDownstream;
    private BedrockClientSession downstream;
    private RequestChunkRadiusPacket requestChunkRadiusPacket;

    private int dimension;

    @Override
    public UUID getUUID() {
        return this.playerData.getUuid();
    }

    @Override
    public String getXuId() {
        return this.getPlayerData().getXuId();
    }

    @Override
    public String getName() {
        return this.getPlayerData().getName();
    }

    @Override
    public void close() {
        if (this.downstream != null) {
            if (!this.downstream.isClosed()) {
                this.downstream.disconnect();
                this.client.close();
            } else {
                ProxyServer.getProxyServer().getLogger().info("Player {} cannot be closed cuz downstream is already closed.", this.getName());
            }
        } else {
            ProxyServer.getProxyServer().getLogger().info("Player {} cannot be closed cuz downstream is null.", this.getName());
        }
    }

    @Override
    public void connect(InetSocketAddress address) {
        this.upstream.setPacketHandler(new ConnectedUpstreamHandler(this));

        ProxyServer.getProxyServer().getLogger().debug("Connecting to downstream server {}", address);
        final BedrockPacketHandler handler;

        if (this.downstream == null) {
            handler = new FirstDownstreamHandler(this);
        } else {
            handler = new SwitchDownstreamHandler(this);
        }

        this.client = this.proxyServer.buildNewClient();
        this.client.connect(address).whenComplete((downstream, throwable) -> { // Testing connect player
            if (throwable != null) {
                ProxyServer.getProxyServer().getLogger().error("Unable to connect to downstream server ", throwable);
                return;
            }

            if (this.downstream == null) {
                this.downstream = downstream;
            } else {
                this.connectDownstream = downstream;
            }

            downstream.setPacketCodec(ProxyServer.BEDROCK_PACKET_CODEC);
            downstream.setPacketHandler(handler);
            downstream.setBatchHandler(new ProxyBatchHandler(this.upstream, "Client-bound"));
            downstream.sendPacketImmediately(this.loginPacket);
            downstream.setLogging(true);

            this.upstream.addDisconnectHandler(reason -> downstream.disconnect());
            this.upstream.setBatchHandler(new ProxyBatchHandler(downstream, "Server-bound"));

            ProxyServer.getProxyServer().getLogger().info(this.getName() + "[" + downstream.getAddress() + "] connected");

            this.initialize();
            this.downstream.setPacketHandler(new ConnectedDownstreamHandler(this));

            this.getProxyServer().getPlayers().put(this.getName(), this);

        });
    }

    @Override
    public void transfer(InetSocketAddress address) {
        this.close();
        this.connect(address);
    }

    @Override
    public boolean dispatchCommand(PlayerCommandSender commandSender, String command) {
        this.getProxyServer().getCommandManager().dispatchCommand(commandSender, command.substring("/".length()));
        return true;
    }

    public void initialize() {
        this.requestChunkRadiusPacket = new RequestChunkRadiusPacket();
        this.requestChunkRadiusPacket.setRadius(8);
    }

    @Override
    public void sendMessage(String message) {
        TextPacket packet = new TextPacket();
        packet.setType(TextPacket.Type.RAW);
        packet.setXuid(this.getXuId());
        packet.setMessage(message);
        this.sendPacket(packet);

    }

    public void sendPacket(BedrockPacket packet) {
        if (this.upstream != null) {
            this.upstream.sendPacket(packet);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }
}
