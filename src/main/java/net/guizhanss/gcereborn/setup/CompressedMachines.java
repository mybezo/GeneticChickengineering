package net.guizhanss.gcereborn.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.items.GCEItems;
import net.guizhanss.gcereborn.items.machines.CompressedExcitationChamber;
import net.guizhanss.gcereborn.items.machines.ExcitationChamber;
import net.guizhanss.guizhanlib.minecraft.utils.ChatUtil;

import lombok.experimental.UtilityClass;

/**
 * Registers the compressed machines (multiple machines combined into one), configured by {@code compressed.yml}.
 * <p>
 * Every machine has a compressed version for each enabled multiplier.
 * The first multiplier is crafted from the original machine, the others from 2 of the previous multiplier
 * in the Compressor. Only the highest multiplier is listed in the guide.
 */
@UtilityClass
public final class CompressedMachines {

    private static final int[] MULTIPLIERS = {8, 16, 32, 64};
    private static final String MACHINES_SECTION = "machines";
    private static final String ID_PREFIX = "GCE_";

    // the energy numbers in the lore, e.g. "&7100 J/s"
    private static final Pattern ENERGY_PATTERN = Pattern.compile("((?:[&§].)*)(\\d+)(?= J)");

    private static final List<SlimefunItemStack> EXCITATION_CHAMBERS = List.of(
        GCEItems.EXCITATION_CHAMBER,
        GCEItems.EXCITATION_CHAMBER_2,
        GCEItems.EXCITATION_CHAMBER_3
    );

    public static void setup(@Nonnull GeneticChickengineering plugin) {
        Config cfg = GeneticChickengineering.getConfigService().getCompressedConfig();

        // add the missing options, existing values are kept
        for (SlimefunItemStack base : EXCITATION_CHAMBERS) {
            cfg.setDefaultValue(getConfigPath(base), true);
        }
        cfg.save();

        if (!cfg.getBoolean("enabled")) {
            return;
        }

        // a multiplier needs all the lower multipliers, since it is crafted from the previous one
        List<Integer> multipliers = new ArrayList<>();
        for (int multiplier : MULTIPLIERS) {
            if (!cfg.getBoolean("multipliers.x" + multiplier)) {
                break;
            }
            multipliers.add(multiplier);
        }
        if (multipliers.isEmpty()) {
            return;
        }

        int count = 0;
        for (SlimefunItemStack base : EXCITATION_CHAMBERS) {
            if (!cfg.getBoolean(getConfigPath(base))) {
                continue;
            }
            SlimefunItem baseItem = SlimefunItem.getById(base.getItemId());
            if (!(baseItem instanceof ExcitationChamber chamber) || baseItem.isDisabled()) {
                continue;
            }

            ItemStack previous = base;
            int previousMultiplier = 1;
            for (int multiplier : multipliers) {
                ItemStack[] recipe = new ItemStack[9];
                recipe[0] = previous.clone();
                recipe[0].setAmount(multiplier / previousMultiplier);

                // only the highest multiplier is listed in the guide, the lower ones are still craftable
                ItemGroup group = multiplier == multipliers.get(multipliers.size() - 1)
                    ? Groups.COMPRESSED_MACHINES
                    : Groups.COMPRESSED_HIDDEN;
                SlimefunItemStack item = createItemStack(chamber, multiplier);

                new CompressedExcitationChamber(group, item, RecipeType.COMPRESSOR, recipe, multiplier)
                    .setCapacity(scale(chamber.getCapacity(), multiplier))
                    .setEnergyConsumption(chamber.getEnergyConsumption())
                    .setProcessingSpeed(chamber.getSpeed())
                    .register(plugin);
                count++;

                previous = item;
                previousMultiplier = multiplier;
            }
        }

        GeneticChickengineering.log(Level.INFO,
            GeneticChickengineering.getLocalization().getString("console.load.compressed", count));
    }

    @Nonnull
    private static String getConfigPath(@Nonnull SlimefunItemStack base) {
        String id = base.getItemId();
        if (id.startsWith(ID_PREFIX)) {
            id = id.substring(ID_PREFIX.length());
        }
        return MACHINES_SECTION + "." + id;
    }

    /**
     * Multiply the value, capped at the integer limit.
     */
    private static int scale(int value, int multiplier) {
        return (int) Math.min(Integer.MAX_VALUE, (long) value * multiplier);
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    private static SlimefunItemStack createItemStack(SlimefunItem base, int multiplier) {
        ItemStack item = base.getItem().clone();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(meta.getDisplayName() + ChatUtil.color(" &a(" + multiplier + ")"));

        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                lore.add(scaleEnergy(line, multiplier));
            }
        }
        lore.add("");
        lore.add(ChatUtil.color(GeneticChickengineering.getLocalization().getString("lores.compressed.machine", multiplier)));
        meta.setLore(lore);
        item.setItemMeta(meta);

        return new SlimefunItemStack(base.getId() + "_X" + multiplier, item);
    }

    /**
     * Multiply the energy number in a lore line.
     */
    @Nonnull
    private static String scaleEnergy(@Nonnull String line, int multiplier) {
        if (!line.contains(" J")) {
            return line;
        }
        Matcher matcher = ENERGY_PATTERN.matcher(line);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(2)) * multiplier;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
