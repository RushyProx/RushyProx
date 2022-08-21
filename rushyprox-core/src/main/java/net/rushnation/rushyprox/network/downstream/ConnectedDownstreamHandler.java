package net.rushnation.rushyprox.network.downstream;

import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.ChangeDimensionPacket;
import com.nukkitx.protocol.bedrock.packet.DisconnectPacket;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.player.ProxyPlayer;

@RequiredArgsConstructor
public class ConnectedDownstreamHandler implements BedrockPacketHandler {

    private final ProxyPlayer player;

    @Override
    public boolean handle(ChangeDimensionPacket packet) {
        this.player.setDimension(packet.getDimension());
        return true;
    }

    @Override
    public boolean handle(DisconnectPacket packet) {
        // Send to fallback

        // Kick
        this.player.close();

        return true;
    }
}
