package net.rushnation.rushyprox.network.upstream;

import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.CommandRequestPacket;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;
import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.command.exception.CommandException;
import net.rushnation.rushyprox.player.ProxyPlayer;

@RequiredArgsConstructor
public class ConnectedUpstreamHandler implements BedrockPacketHandler {

    private final ProxyPlayer player;

    @Override
    public boolean handle(RequestChunkRadiusPacket packet) {
        this.player.setRequestChunkRadiusPacket(packet);
        return true;
    }

    @Override
    public final boolean handle(CommandRequestPacket packet) {
        return this.player.dispatchCommand(this.player, packet.getCommand());
    }
}