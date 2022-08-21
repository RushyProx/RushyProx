package net.rushnation.rushyprox.player;

import net.rushnation.rushyprox.command.type.PlayerCommandSender;

import java.net.InetSocketAddress;
import java.util.UUID;

public interface IProxyPlayer {

    /**
     *
     * @return The uuid of the player {@link UUID}
     */
    UUID getUUID();

    /**
     *
     * @return The xuid of the player {@link String}
     */
    String getXuId();

    /**
     *
     * @return The name of the player {@link String}
     */
    String getName();

    /**
     * close the connection of the player
     */
    void close();

    /**
     * Connect the player to a server
     * @param address The address of the server {@link InetSocketAddress}
     */
    void connect(InetSocketAddress address);

    /**
     * Transfer the player to a server
     * @param address The address of the server {@link InetSocketAddress}
     */
    void transfer(InetSocketAddress address);

    /**
     *  @param commandSender
     * @param command
     * @return
     */
    boolean dispatchCommand(PlayerCommandSender commandSender, String command);

}
