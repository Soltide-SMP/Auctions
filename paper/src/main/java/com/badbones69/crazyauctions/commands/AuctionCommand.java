package com.badbones69.crazyauctions.commands;

import com.badbones69.crazyauctions.Methods;
import com.badbones69.crazyauctions.api.enums.Category;
import com.badbones69.crazyauctions.api.enums.other.Permissions;
import com.badbones69.crazyauctions.controllers.GuiListener;
import com.badbones69.crazyauctions.common.enums.FileKey;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.ryderbelserion.fusion.kyori.permissions.PermissionContext;
import com.ryderbelserion.fusion.paper.builders.commands.context.PaperCommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import us.crazycrew.api.constants.Messages;
import us.crazycrew.api.enums.ShopType;
import java.util.List;

public class AuctionCommand extends BaseCommand {

    @Override
    public void run(@NonNull final PaperCommandContext context) {
        final CommandSender sender = context.getSender();

        if (!context.isPlayer()) {
            this.adapter.sendMessage(sender, Messages.players_only);

            return;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> {
                if (!Methods.hasPermission(sender, "access")) {
                    return true;
                }

                sender.sendMessage(Messages.HELP.getMessage(sender));

                return true;
            }

            case "reload" -> {
                if (!Methods.hasPermission(sender, "reload")) {
                    return true;
                }

                this.fileManager.reloadFiles().init();

                this.crazyManager.load();

                sender.sendMessage(Messages.RELOAD.getMessage(sender));

                return true;
            }

            case "force_end_all" -> {
                if (!Methods.hasPermission(sender, "force-end-all")) {
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));
                    return true;
                }

                forceEndAll(player);

                return true;
            }

            case "view" -> {
                if (!Methods.hasPermission(sender, "view")) {
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));

                    return true;
                }

                if (args.length >= 2) {
                    GuiListener.openViewer(player, args[1], 1);

                    return true;
                }

                sender.sendMessage(Messages.CRAZYAUCTIONS_VIEW.getMessage(sender));

                return true;
            }

            case "expired", "collect" -> {
                if (!Methods.hasPermission(sender, "access")) {
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));

                    return true;
                }

                GuiListener.openPlayersExpiredList(player, 1);

                return true;
            }

            case "listed" -> {
                if (!Methods.hasPermission(sender, "access")) return true;

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));

                    return true;
                }

                GuiListener.openPlayersCurrentList(player, 1);

                return true;
            }

            case "sell", "bid" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));

                    return true;
                }

                if (args.length >= 2) {
                    if (args[0].equalsIgnoreCase("sell")) {
                        if (!crazyManager.isSellingEnabled()) {
                            player.sendMessage(Messages.SELLING_DISABLED.getMessage(sender));

                            return true;
                        }

                        if (!Methods.hasPermission(player, "sell")) return true;
                    }

                    if (args[0].equalsIgnoreCase("bid")) {
                        if (!crazyManager.isBiddingEnabled()) {
                            player.sendMessage(Messages.BIDDING_DISABLED.getMessage(sender));

                            return true;
                        }

                        if (!Methods.hasPermission(player, "bid")) return true;
                    }

                    ItemStack item = Methods.getItemInHand(player);
                    int amount = item.getAmount();

                    if (args.length >= 3) {
                        if (!Methods.isInt(args[2])) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%Arg%", args[2]);
                            placeholders.put("%arg%", args[2]);

                            player.sendMessage(Messages.NOT_A_NUMBER.getMessage(sender, placeholders));

                            return true;
                        }

                        amount = Integer.parseInt(args[2]);

                        if (amount <= 0) amount = 1;
                        if (amount > item.getAmount()) amount = item.getAmount();
                    }

                    String stringPrice = args[1].toLowerCase()
                            .replaceAll("tn", "000000000000")
                            .replaceAll("bn", "000000000")
                            .replaceAll("m", "000000")
                            .replaceAll("k", "000");

                    if (!Methods.isLong(stringPrice)) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%Arg%", stringPrice);
                        placeholders.put("%arg%", stringPrice);

                        player.sendMessage(Messages.NOT_A_NUMBER.getMessage(sender, placeholders));

                        return true;
                    }

                    if (Methods.getItemInHand(player).getType() == Material.AIR) {
                        player.sendMessage(Messages.DOESNT_HAVE_ITEM_IN_HAND.getMessage(sender));

                        return false;
                    }
                    if (Methods.getItemInHand(player).getType() == Material.FILLED_MAP) {
                        player.sendMessage(Messages.ITEM_BLACKLISTED.getMessage(sender));

                        return false;
                    }

                    long price = Long.parseLong(stringPrice);

                    if (args[0].equalsIgnoreCase("bid")) {
                        if (price < config.getLong("Settings.Minimum-Bid-Price", 100)) {
                            player.sendMessage(Messages.BID_PRICE_TO_LOW.getMessage(sender));

                            return true;
                        }

                        if (price > config.getLong("Settings.Max-Beginning-Bid-Price", 1000000)) {
                            player.sendMessage(Messages.BID_PRICE_TO_HIGH.getMessage(sender));

        final Player player = context.getPlayer();

        if (config.getBoolean("Settings.Category-Page-Opens-First", false)) {
            GuiListener.openCategories(player, ShopType.SELL);

            return;
        }

        if (this.platform.isSellModuleEnabled()) {
            GuiListener.openShop(player, ShopType.SELL, Category.NONE, 1);
        } else if (this.platform.isBidModuleEnabled()) {
            GuiListener.openShop(player, ShopType.BID, Category.NONE, 1);
        } else {
            player.sendMessage(Methods.getPrefix() + Methods.color("&cThe bidding and selling options are both disabled. Please contact the admin about this."));
        }
    }

    @Override
    public @NonNull final List<PermissionContext> getPermissions() {
        return List.of(Permissions.access.getContext());
    }

    @Override
    public final boolean requirement(@NotNull final CommandSourceStack context) {
        return context.getSender().hasPermission(getPermissions().getFirst().getPermission());
    }

    @Override
    public @NotNull final LiteralCommandNode<CommandSourceStack> literal() {
        return Commands.literal("crazyauctions").executes(context -> {
            run(new PaperCommandContext(context));

            return Command.SINGLE_SUCCESS;
        }).requires(this::requirement).build();
    }

    @Override
    public @NonNull final List<String> getAliases() {
        return List.of("ah", "ca");
    }
}