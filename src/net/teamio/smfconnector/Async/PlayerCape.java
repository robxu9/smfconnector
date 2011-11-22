package net.teamio.smfconnector.Async;

import net.teamio.ThreadHelper;

import org.bukkit.entity.Player;

import mmo.Core.MMOPlugin;


public class PlayerCape extends MMOPlugin{

	public static void changeCape(Player player, String url, boolean noise, boolean verbose){
		final ThreadHelper th = new ThreadHelper("SMFCon");
		if (noise)
			th.print("Attempting to set cape of player " + player.getName(), 0);
		if (verbose)
			th.print(player,"Attempting to set your cape...");
		MMOPlugin.mmoCore.setCloak(player, url);
		if (noise)
			th.print("Set " + player.getName() + "'s cape successfully.", 0);
		if (verbose)
			th.print(player,"Set your cape successfully!");
	}

}
