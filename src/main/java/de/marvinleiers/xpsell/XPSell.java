package de.marvinleiers.xpsell;

import de.marvinleiers.xpsell.order.Order;
import net.md_5.bungee.api.chat.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

import static net.md_5.bungee.api.chat.ClickEvent.*;

public final class XPSell extends JavaPlugin
{
    private static Economy econ;
    private HashMap<Player, Order> orders = new HashMap<>();

    @Override
    public void onEnable()
    {
        if (!setupEconomy())
        {
            System.out.println(String.format("[%s] - §4Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("xpsell").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("§cNur für Spieler!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1)
        {
            if (args[0].equalsIgnoreCase("accept"))
            {
                if (!orders.containsKey(player))
                {
                    player.sendMessage("§cDir liegt kein offenes Angebot vor!");
                    return true;
                }

                Order order = orders.get(player);

                if (!order.confirm())
                {
                    order.getSeller().sendMessage("§cFehler: Verkauf abgebrochen. Du hast nicht genügend Level oder der Käufer besitzt nicht genügend Geld.");
                    order.getBuyer().sendMessage("§cFehler: Kauf abgebrochen. Du hast nicht genügend Geld oder der Verkäufer besitzt nicht genügend Level.");
                }
                else
                {
                    order.getSeller().sendMessage("§aVerkauf wurde erfolgreich durchgeführt!");
                    order.getBuyer().sendMessage("§aKauf wurde erfolgreich durchgeführt!");
                }

                orders.remove(player);
            }
            else if (args[0].equalsIgnoreCase("info"))
            {
                sendMessage(player, "§eXPSell Plugin version: " + this.getDescription().getVersion() + " by §e§l§nMarvin2k0");
            }
            else
            {
                player.sendMessage(" ");
                player.sendMessage("§cUsage: /xpsell info");
                player.sendMessage("§cUsage: /xpsell <buyer> <level> <preis>");
                player.sendMessage("§cUsage: /xpsell accept");
            }

            return true;
        }

        if (args.length != 3)
        {
            player.sendMessage(" ");
            player.sendMessage("§cUsage: /xpsell info");
            player.sendMessage("§cUsage: /xpsell <buyer> <level> <preis>");
            player.sendMessage("§cUsage: /xpsell accept");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null)
        {
            player.sendMessage("§7" + args[0] + " §cist nicht online!");
            return true;
        }

        int level = 0;
        double price = 0;

        try
        {
            level = Integer.parseInt(args[1]);

            if (level <= 0)
            {
                player.sendMessage("§e" + args[1] + " ist kein erlaubter Wert für \"level\"");
                return true;
            }
        }
        catch (Exception e)
        {
            player.sendMessage("§e" + args[1] + " ist kein erlaubter Wert für \"level\"");
            return true;
        }

        try
        {
            args[2] = args[2].replace(",", ".");
            price = Double.parseDouble(args[2]);

            if (price <= 0)
            {
                player.sendMessage("§e" + args[2] + " ist kein erlaubter Wert für \"preis\"");
                return true;
            }
        }
        catch (Exception e)
        {
            player.sendMessage("§e" + args[2] + " ist kein erlaubter Wert für \"preis\"");
            return true;
        }

        if (player.getLevel() < level)
        {
            player.sendMessage("§cDafür fehlen dir §7" + (level - player.getLevel()) + " Level");
            return true;
        }

        Order order = new Order(player, target, level, price);
        orders.put(order.getBuyer(), order);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (order.hasEnded())
                {
                    this.cancel();
                    return;
                }

                order.getBuyer().sendMessage("§cAngebot beendet!");
                orders.remove(order.getBuyer());
            }
        }.runTaskLater(this, 60 * 20);

        return true;
    }

    public void sendMessage(Player p, String msg)
    {
        BaseComponent message = new TextComponent(msg);
        message.setClickEvent(new ClickEvent( Action.OPEN_URL, "http://www.marvinleiers.de" ));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me!").create()));
        p.spigot().sendMessage(message);
    }

    public static Economy getEconomy()
    {
        return econ;
    }

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
        {
            return false;
        }

        econ = rsp.getProvider();
        return econ != null;
    }
}
