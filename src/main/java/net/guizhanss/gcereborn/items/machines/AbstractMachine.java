package net.guizhanss.gcereborn.items.machines;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

import net.guizhanss.gcereborn.GeneticChickengineering;

public abstract class AbstractMachine extends AContainer {

    protected static final int INFO_SLOT = 22;

    private final String identifier;

    @ParametersAreNonnullByDefault
    protected AbstractMachine(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        identifier = item.getItemId();
    }

    @Override
    @Nonnull
    public String getMachineIdentifier() {
        return identifier;
    }

    /**
     * Play the chicken death sound at the machine.
     * Machines may tick asynchronously, so the sound is played on the main thread.
     *
     * @param menu The {@link BlockMenu} of the machine.
     */
    protected static void playDeathSound(@Nonnull BlockMenu menu) {
        Location l = menu.getLocation();
        GeneticChickengineering.getScheduler().run(() ->
            l.getWorld().playSound(l, Sound.ENTITY_CHICKEN_DEATH, 1f, 1f)
        );
    }
}
