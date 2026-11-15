package net.guizhanss.gcereborn.items.machines;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.items.GCEItems;
import net.guizhanss.gcereborn.utils.ChickenUtils;

public class RestorationChamber extends AbstractMachine {

    public RestorationChamber(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    @Nonnull
    public ItemStack getProgressBar() {
        return GCEItems.POCKET_CHICKEN.clone();
    }

    @Override
    @Nullable
    protected MachineRecipe findNextRecipe(@Nonnull BlockMenu menu) {
        var config = GeneticChickengineering.getConfigService();
        ItemStack chicken = null;
        ItemStack seed = null;
        for (int slot : getInputSlots()) {
            ItemStack item = menu.getItemInSlot(slot);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (ChickenUtils.isPocketChicken(item) && ChickenUtils.hasEntityData(item)) {
                chicken = item;
            } else if (ChickenUtils.isFood(item)) {
                seed = item;
            }
        }

        if (chicken == null || seed == null) {
            return null;
        }

        double health = ChickenUtils.getHealth(chicken);
        if (health >= 4d) {
            return null;
        }
        // each seed restores 0.25 health
        int toConsume = Math.min(seed.getAmount(), (int) Math.ceil((4d - health) / 0.25));
        if (toConsume <= 0) {
            return null;
        }
        ItemStack recipeSeeds = seed.clone();
        recipeSeeds.setAmount(toConsume);
        // only one chicken is consumed, so only one chicken should be output
        ItemStack inputChick = chicken.clone();
        inputChick.setAmount(1);
        ItemStack recipeChick = inputChick.clone();
        ChickenUtils.heal(recipeChick, toConsume * 0.25);
        MachineRecipe recipe = new MachineRecipe(
            config.isTest() ? 1 : config.getHealRate() * toConsume,
            new ItemStack[] {recipeSeeds, inputChick},
            new ItemStack[] {recipeChick}
        );
        if (!InvUtils.fitAll(menu.toInventory(), recipe.getOutput(), getOutputSlots())) {
            return null;
        }
        ItemUtils.consumeItem(chicken, false);
        ItemUtils.consumeItem(seed, toConsume, false);
        return recipe;
    }
}
