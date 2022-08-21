package net.rushnation.rushyprox.network.downstream;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.*;
import net.rushnation.rushyprox.player.ProxyPlayer;

public class SwitchDownstreamHandler extends FirstDownstreamHandler {

    public SwitchDownstreamHandler(ProxyPlayer player) {
        super(player);
    }

    @Override
    public boolean handle(ResourcePacksInfoPacket packet) {
        ResourcePackClientResponsePacket clientResponsePacket = new ResourcePackClientResponsePacket();
        clientResponsePacket.setStatus(ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS);
        this.player.getConnectDownstream().sendPacket(clientResponsePacket);
        return true;
    }

    @Override
    public boolean handle(ResourcePackStackPacket packet) {
        ResourcePackClientResponsePacket clientResponsePacket = new ResourcePackClientResponsePacket();
        clientResponsePacket.setStatus(ResourcePackClientResponsePacket.Status.COMPLETED);
        this.player.getConnectDownstream().sendPacket(clientResponsePacket);
        return true;
    }

    @Override
    public boolean handle(StartGamePacket packet) {
        SetPlayerGameTypePacket modePacket = new SetPlayerGameTypePacket();
        modePacket.setGamemode(packet.getPlayerGameType().ordinal());
        this.player.getUpstream().sendPacket(modePacket);

        SetLocalPlayerAsInitializedPacket initializedPacket = new SetLocalPlayerAsInitializedPacket();
        initializedPacket.setRuntimeEntityId(packet.getRuntimeEntityId());
        this.player.getConnectDownstream().sendPacket(initializedPacket);

        MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
        movePlayerPacket.setMode(MovePlayerPacket.Mode.RESPAWN);
        movePlayerPacket.setRotation(Vector3f.from(packet.getRotation().getX(), packet.getRotation().getY(), packet.getRotation().getY()));
        movePlayerPacket.setPosition(packet.getPlayerPosition());
        movePlayerPacket.setRuntimeEntityId(packet.getRuntimeEntityId());
        this.player.getUpstream().sendPacket(movePlayerPacket);

        this.player.getConnectDownstream().sendPacket(this.player.getRequestChunkRadiusPacket());
        this.player.getConnectDownstream().setPacketHandler(new ConnectedDownstreamHandler(this.player));
        return true;
    }
}
