package de.marvinleiers.xpsell.order;

import de.marvinleiers.xpsell.XPSell;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class Order
{
    private static final DecimalFormat format = new DecimalFormat("##.##");

    private Player seller;
    private Player buyer;
    String  level;
    String  price;
    private boolean ended;

    public Order(Player seller, Player buyer, int level, double price)
    {
        this.seller = seller;
        this.buyer = buyer;
        this.level = format.format(level);
        this.price = format.format(price);
        this.ended = false;

        seller.sendMessage("§aDu hast ein Angebot von §2" + this.price + "€ §afür §2" + this.level + " Level §aan §2" + buyer.getName() + " §agesendet!");
        buyer.sendMessage("§2" + buyer.getName() + " §amöchte dir §2" + this.level + " Level §afür §2" + this.price + "€ §averkaufen. §2/xpsell accept§a, um das Angebot anzunehmen.");
    }

    public boolean hasEnded()
    {
        return ended;
    }

    public boolean confirm()
    {
        this.ended = true;

        if (getSeller().getLevel() < getLevel())
            return false;

        if (XPSell.getEconomy().getBalance(getBuyer()) < getPrice())
            return false;

        XPSell.getEconomy().withdrawPlayer(getBuyer(), getPrice());
        getBuyer().setLevel(getBuyer().getLevel() + getLevel());

        XPSell.getEconomy().depositPlayer(getSeller(), getPrice());
        getSeller().setLevel(getSeller().getLevel() - getLevel());

        ended = true;

        return true;
    }

    public Player getSeller()
    {
        return seller;
    }

    public Player getBuyer()
    {
        return buyer;
    }

    public int getLevel()
    {
        return Integer.parseInt(level);
    }

    public double getPrice()
    {
        return Double.parseDouble(price.replace(",", "."));
    }
}
