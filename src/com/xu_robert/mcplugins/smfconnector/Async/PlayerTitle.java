package com.xu_robert.mcplugins.smfconnector.Async;

import mmo.Core.MMOPlugin;

import org.bukkit.entity.Player;

import com.xu_robert.mcplugins.smfconnector.ThreadHelper;

import net.milkbowl.vault.chat.Chat;

public class PlayerTitle extends MMOPlugin{
	
	private ThreadHelper th = new ThreadHelper();
	private Player p;
	private String t;
	private Chat c;

	public PlayerTitle(Player player, String title, Chat chat) {
		p=player;
		t=title;
		c=chat;
	}

	public void changeTitle(final boolean noise, final boolean verbose){
		if (noise)
			th.print("Attempting to set title of player " + p.getName(), 0);
		if (verbose)
			th.print(p,"Attempting to set your title...");
		MMOPlugin.mmoCore.setTitle(p, t + " " + p.getName());
		
		String prefix = "["+c.getGroupPrefix(p.getWorld(), c.getPrimaryGroup(p))+"] ";
		prefix+=title + " ";
		
		c.setPlayerPrefix(p, prefix);

		if (noise)
			th.print("Set " + p.getName() + "'s title successfully.", 0);
		if (verbose)
			th.print(p,"Set your title successfully!");
	}

}
