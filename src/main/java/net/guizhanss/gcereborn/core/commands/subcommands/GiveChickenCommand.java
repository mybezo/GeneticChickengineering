package net.guizhanss.gcereborn.core.commands.subcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.core.commands.AbstractSubCommand;
import net.guizhanss.gcereborn.core.genetics.DNA;
import net.guizhanss.gcereborn.items.chicken.ChickenTypes;
import net.guizhanss.gcereborn.utils.ChickenUtils;
import net.guizhanss.guizhanlib.minecraft.commands.AbstractCommand;
import net.guizhanss.guizhanlib.minecraft.utils.InventoryUtil;

/**
 * Gives the sender an adult pocket chicken of the given type, with known (pure) DNA.
 */
public final class GiveChickenCommand extends AbstractSubCommand {

    public GiveChickenCommand(@Nonnull AbstractCommand parent) {
        super(parent, "give", "<type>");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onExecute(CommandSender sender, String[] args) {
        var localization = GeneticChickengineering.getLocalization();
        if (!hasPermission(sender)) {
            localization.sendMessage(sender, "no-permission");
            return;
        }
        if (!(sender instanceof Player p)) {
            localization.sendMessage(sender, "no-console");
            return;
        }
        if (args.length < 1) {
            localization.sendMessage(sender, "usage", "/gce give <type>");
            return;
        }

        Integer typing = ChickenTypes.getTypingByName(args[0]);
        if (typing == null) {
            localization.sendMessage(sender, "invalid-chicken-type", args[0]);
            return;
        }

        ItemStack chicken = ChickenUtils.fromDNA(new DNA(typing), false);
        InventoryUtil.push(p, chicken);
        localization.sendMessage(sender, "chicken-given", ChickenTypes.getDisplayName(typing));
    }

    @Override
    @ParametersAreNonnullByDefault
    public List<String> onTab(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String input = args[0].toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String name : ChickenTypes.getNames()) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.startsWith(input)) {
                result.add(lower);
            }
        }
        return result;
    }
}
