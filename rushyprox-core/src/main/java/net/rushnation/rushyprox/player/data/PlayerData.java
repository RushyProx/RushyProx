package net.rushnation.rushyprox.player.data;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PlayerData {

    private final UUID uuid;
    private final String XuId;
    private final String name;

}
