package net.guizhanss.gcereborn.utils;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Heads {
    // https://minecraft-heads.com/custom-heads/animals/336-chicken
    CHICKEN("1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893"),
    // Mybezo's skin, the icon of "Compressed Items"
    MYBEZO("82600941fab88efc41bb132d873d3b5c5f35f7720ebd9375476c1a18955bb03e");

    private final String texture;

    public ItemStack getItem() {
        return SlimefunUtils.getCustomHead(texture);
    }
}
