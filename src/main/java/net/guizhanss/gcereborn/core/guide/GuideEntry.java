package net.guizhanss.gcereborn.core.guide;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;

/**
 * An entry of a {@link GuideMenuGroup}: the displayed item and its click handler.
 *
 * @param item    The displayed {@link ItemStack}.
 * @param handler The {@link MenuClickHandler}.
 */
public record GuideEntry(@Nonnull ItemStack item, @Nonnull MenuClickHandler handler) {
}
