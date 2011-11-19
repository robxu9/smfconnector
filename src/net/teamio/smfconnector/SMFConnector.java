package net.teamio.smfconnector;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import mmo.Core.MMOPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.teamio.smfconnector.Async.PlayerCape;
import net.teamio.smfconnector.Async.PlayerSkin;
import net.teamio.smfconnector.Async.PlayerTitle;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.util.config.ConfigurationException;


import lib.PatPeter.SQLibrary.MySQL;

/**
 * SMFConnector connects a bukkit server with the 
 * Simple Machines Forum (2.0.1+). It uses custom fields 
 * and MySQL in order to reflect changes.
 * <br>
 * Requires the use of mmoCore from mmoMinecraft.
 * @author xu_robert
 *
 */
public class SMFConnector extends MMOPlugin{

	protected final ThreadHelper th = new ThreadHelper();
	private final SMFConnectorPlayerListener playerListener = new SMFConnectorPlayerListener(this);
	private PluginManager pm = this.getServer().getPluginManager();
	protected boolean noise;
	protected boolean verbose;
	public MySQL connection;
	public static Permission permission = null;
	public static Chat chat = null;

	/**
	 * Disables connection to MySQL and shuts down.
	 */
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		th.print("SMFConnector shutdown call caught, disabling...",1);
		th.print("Closing connection to MySQL database...",0);
		connection.close();
		th.print("Closed SMFConnector version " + this.getDescription().getVersion(),1);
	}

	/**
	 * Sets up configuration and linking
	 */
	@Override
	public void onEnable() {
		th.print("Starting up SMFConnector version " + this.getDescription().getVersion(),0);
		if (!setupPermission()){
			th.print("Vault failed to setup permissions, disabling.",-1);
			onDisable();
		}
		else if (!setupChat()){
			th.print("Vault failed to setup chat, disabling.",-1);
			onDisable();
		}
		else{
			th.print("Linked with Permissions, Chat, and Economy plugins.",0);
			/* setup methods */
			try {
				boolean defsetup = setupDefaults();
				if (defsetup){
					th.print("New configuration has been written, disabling to avoid errors.",1);
					th.print("Modify the new configuration and restart the server to load.",1);
					onDisable();
				}
				else if (!defsetup){
					th.print("Configuration loaded.", 0);
					if (!setupMySQL()){
						th.print("Failed to establish a connection to MySQL, disabling.",-1);
						onDisable();
					}
					else if (!setupConfig()){
						th.print("Could not read defaults.yml, disabling.",-1);
						onDisable();
					}
					else if (!setupListener()){
						th.print("Failed to setup listeners, disabling.",-1);
						onDisable();
					}
				}
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				th.print("Could not write/load configuration! Do you have permissions?", -1);
				onDisable();
			}
		}
		// establish a connection to the MySQL database

	}


	/**
	 * When a player calls /smfcon, this is what they will see.
	 * @return true always because we don't like bukkit's crappy menu
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player=null;
		if (sender instanceof Player)
			player = (Player) sender;

		if (command.getName().equalsIgnoreCase("smfcon")){
			// commands follow below
			/* sync */
			if (args[0].equals("sync")){
				if (player.equals(null)){
					th.print("Use /smfcon syncall on the console, kthnx.",1);
				}
				else{
					// sync for the person who called this, if they have perms
					if (permission.playerHas(player.getWorld(),player.getName(),"smfcon.sync")){
						if(permission.playerHas(player.getWorld(),player.getName(),"smfcon.cape"))
							schedCapeChange(player);
						if(permission.playerHas(player.getWorld(),player.getName(),"smfcon.skin"))
							schedSkinChange(player);
						if(permission.playerHas(player.getWorld(),player.getName(),"smfcon.title"))
							schedTitleChange(player);
					}
					else
						th.print(player,ChatColor.RED + "You don't have permissions to sync.");
				}
			}
			else if (args[0].equals("syncall")){
				if (player.equals(null) || permission.playerHas(player.getWorld(),player.getName(),"smfcon.syncall")){
					th.print(player, "Syncing all ONLINE players now.");
					Player[] list = this.getServer().getOnlinePlayers();
					for (int i=0; i<list.length; i++){
						th.print(player, "Syncing " + list[i].getName() + "...");
						if(permission.playerHas(list[i].getWorld(),list[i].getName(),"smfcon.cape"))
							schedCapeChange(list[i]);
						if(permission.playerHas(list[i].getWorld(),list[i].getName(),"smfcon.skin"))
							schedSkinChange(list[i]);
						if(permission.playerHas(list[i].getWorld(),list[i].getName(),"smfcon.cape"))
							schedTitleChange(list[i]);
					}
					th.print(player, "Done syncing all online players.");
				}
				else
					th.print(player,ChatColor.RED + "You don't have permissions to sync.");
			}
			else if (args[0].equals("help")){
				showHelp(player);
			}
			else{
				th.print(player,"I have no idea what you're looking for...");
				showHelp(player);
			}
		}

		/* always return true, our interface is awesome. */
		return true;

	}

	/**
	 * Shows the help menu. Customized with permissions.
	 * @param player player to examine
	 */
	private void showHelp(Player player){
		th.print(player,ChatColor.GREEN+"smfconnector help:");
		th.print(player,"");
		if (permission.playerHas(player.getWorld(),player.getName(),"smfcon.sync")){
			th.print(player,"/smfcon sync : sync your user stats with the forum");
			if(permission.playerHas(player.getWorld(),player.getName(),"smfcon.cape"))
				th.print(player,"           : cape is syncable");
			if(permission.playerHas(player.getWorld(),player.getName(),"smfcon.skin"))
				th.print(player,"           : skin is syncable");
			if(permission.playerHas(player.getWorld(),player.getName(),"smfcon.title"))
				th.print(player,"           : title is syncable");
		}
		if (player.equals(null) || permission.playerHas(player.getWorld(),player.getName(),"smfcon.syncall"))
			th.print(player,"/smfcon syncall : sync everyone's stats with the forum");
		th.print(player, "/smfcon help : show this message");
		th.print(player, "");
		if (!player.equals(null))
			th.print(player, "don't see any options? you might not have permission to see them.");
		else
			th.print(player, "that's right. only syncall can be done from the console.");
		th.print(player, "tip: don't see your skin/cape? you might have specified an invalid file.");
		
	}
	
	/**
	 * Uses regex to check a good png file
	 * @param url url with png
	 * @return true if it is a png file (extension-wise)
	 */
	private boolean pngvalidate(final String url){
		Pattern pattern = Pattern.compile("([^\\s]+(\\.(?i)(png))$)");
		return pattern.matcher(url).matches();
	}
	
	/**
	 * Schedule a cape change for the player
	 * @param player player to sync capes
	 * @return true if success
	 */
	protected boolean schedCapeChange(final Player player){
		if(!permission.playerHas(player.getWorld(),player.getName(),"smfcon.cape"))
			return false;
		ResultSet url = connection
				.query("SELECT * FROM `smf_themes` WHERE `id_member` = (SELECT `id_member` FROM `smf_members` WHERE `member_name` = '"
						+ player.getName()
						+ "') AND `variable` = '"
						+ th.defaults.getString("tables.cape") + "'LIMIT 1");
		String surl;
		try{
			surl = url.getString("value");
			if (!pngvalidate(surl))
				return false;
		} catch (SQLException e){
			return false;
		}
		schedCapeChange(player,surl);
		return true;
	}
	
	/**
	 * Perform the cape change with the player and the URL.
	 * @param player player to sync capes
	 * @param url image file url
	 */
	private void schedCapeChange(final Player player, final String url){
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				PlayerCape usercape = new PlayerCape(player,url);
				usercape.changeCape(noise, verbose);
			}
		}, 0L);
	}
	
	protected boolean schedSkinChange(final Player player){
		if(!permission.playerHas(player.getWorld(),player.getName(),"smfcon.skin"))
			return false;
		ResultSet url = connection
				.query("SELECT * FROM `smf_themes` WHERE `id_member` = (SELECT `id_member` FROM `smf_members` WHERE `member_name` = '"
						+ player.getName()
						+ "') AND `variable` = '"
						+ th.defaults.getString("tables.skin") + "'LIMIT 1");
		String surl;
		try{
			surl = url.getString("value");
			if (!pngvalidate(surl))
				return false;
		} catch (SQLException e){
			return false;
		}
		schedSkinChange(player,surl);
		return true;
	}
	
	private void schedSkinChange(final Player player, final String url){
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				PlayerSkin userskin = new PlayerSkin(player,url);
				userskin.changeSkin(noise, verbose);
			}
		}, 0L);
	}
	
	protected boolean schedTitleChange(final Player player){
		if(!permission.playerHas(player.getWorld(),player.getName(),"smfcon.title"))
			return false;
		ResultSet heading = connection
				.query("SELECT * FROM `smf_themes` WHERE `id_member` = (SELECT `id_member` FROM `smf_members` WHERE `member_name` = '"
						+ player.getName()
						+ "') AND `variable` = '"
						+ th.defaults.getString("tables.title") + "'LIMIT 1");
		String shead;
		try{
			shead = heading.getString("value");
			if (shead.length()>16)
				return false;
		} catch (SQLException e){
			return false;
		}
		schedTitleChange(player,shead);
		return true;
	}
	
	private void schedTitleChange(final Player player, final String heading){
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				PlayerTitle newtitle = new PlayerTitle(player,heading,chat);
				newtitle.changeTitle(noise, verbose);
			}
		}, 0L);
	}

	
	private boolean setupPermission(){
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}

		return (permission != null);
	}
	private boolean setupChat(){
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}

		return (chat != null);
	}

	// did we have to setup defaults?
	private boolean setupDefaults() throws ConfigurationException{
		// we usually don't have to fix defaults, but on first run, we do
		boolean setup = false;

		/* mysql configuration */
		try{
			th.mysqlconfig = getConfig();
			th.mysqlconfigfile = new File(th.defdir + "mysql.yml");
			th.mysqlconfigfile.mkdir();
			if (!th.mysqlconfig.contains("prefix")){
				th.mysqlconfig.set("prefix", "smf_");
				setup = true;
			}
			if (!th.mysqlconfig.contains("hostname")){
				th.mysqlconfig.set("hostname", "localhost");
				setup = true;
			}
			if (!th.mysqlconfig.contains("port")){
				th.mysqlconfig.set("port", "3306");
				setup = true;
			}
			if (!th.mysqlconfig.contains("database")){
				th.mysqlconfig.set("database", "forum");
				setup = true;
			}
			if (!th.mysqlconfig.contains("username")){
				th.mysqlconfig.set("username", "root");
				setup = true;
			}
			if (!th.mysqlconfig.contains("password")){
				th.mysqlconfig.set("password", "toor");
				setup = true;
			}
			saveConfig();
		}catch(Exception e1){
			e1.printStackTrace();
			throw new ConfigurationException("Could not set defaults!");
		}
		
		/* defaults configuration */
		try{
			th.defaults = getConfig();
			th.defaultsfile = new File(th.defdir + "defaults.yml");
			th.defaultsfile.mkdir();
			if (!th.defaults.contains("noise")){
				th.defaults.set("noise", false);
				setup = true;
			}
			if (!th.defaults.contains("verbose")){
				th.defaults.set("verbose",false);
				setup = true;
			}
			if (!th.defaults.contains("tables.cape")){
				th.defaults.set("tables.cape","cust_capeur");
				setup = true;
			}
			if (!th.defaults.contains("tables.skin")){
				th.defaults.set("tables.skin", "cust_skinur");
				setup = true;
			}
			if (!th.defaults.contains("tables.title")){
				th.defaults.set("tables.title", "cust_minecr");
				setup = true;
			}
			saveConfig();
		}catch(Exception e1){
			e1.printStackTrace();
			throw new ConfigurationException("Could not set defaults!");
		}
		
		/* title configuration */

		return setup;
	}


	private boolean setupMySQL() {
		connection = new MySQL(th.log, th.mysqlconfig.getString("prefix"),
				th.mysqlconfig.getString("hostname"),
				th.mysqlconfig.getString("port"),
				th.mysqlconfig.getString("database"),
				th.mysqlconfig.getString("username"),
				th.mysqlconfig.getString("password"));
		return connection.checkConnection();
	}
	
	private boolean setupConfig(){
		try{
			noise = th.defaults.getBoolean("noise");
			verbose = th.defaults.getBoolean("verbose");
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean setupListener(){
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_CHANGED_WORLD, playerListener, Event.Priority.Normal, this);
		return true;
	}


}
