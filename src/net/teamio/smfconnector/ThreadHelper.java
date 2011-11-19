package net.teamio.smfconnector;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ThreadHelper {

	protected Logger log = Logger.getLogger("Minecraft");
	protected String defdir = "plugins" + File.separator + "SMFConnector" + File.separator;
	/* configuration files */
	protected FileConfiguration mysqlconfig;
	protected File mysqlconfigfile;
	protected FileConfiguration defaults;
	protected File defaultsfile;
	
	// 0 = info
	// 1 = warning
	// -1 = severe
	public void print(String message, int level){
		print(null,message,level);
	}
	
	/* implied info */
	public void print(Player player, String message){
		print(player,message,0);
	}
	
	// ChatColor.<COLOUR> is a string.
	public void print(Player player, String message, int level){
		if (!player.equals(null)){
			player.sendMessage(message);
		}
		else{
			if (level==1)
				log.warning("[SMFCon] " + message);
			else if (level==-1)
				log.severe("[SMFCon ~] " + message);
			else
				log.info("[SMFCon !] " + message);
		}
	}
}
