package dev.doctor4t.trainmurdermystery.api;

import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * @author canyuesama
 */
public interface RoleComponent extends AutoSyncedComponent {
    Player getPlayer();
    void reset();
    void clear();

}
