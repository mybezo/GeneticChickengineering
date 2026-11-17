package net.guizhanss.gcereborn.core.guide;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;

/**
 * An {@link ItemGroup} that is not listed in the survival guide.
 * Its items are displayed by a {@link ItemListGroup} instead, or not at all.
 * The cheat menu still lists it.
 */
public class HiddenItemGroup extends ItemGroup {

    @ParametersAreNonnullByDefault
    public HiddenItemGroup(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    @Override
    public boolean isVisible(@Nonnull Player p) {
        return false;
    }
}
