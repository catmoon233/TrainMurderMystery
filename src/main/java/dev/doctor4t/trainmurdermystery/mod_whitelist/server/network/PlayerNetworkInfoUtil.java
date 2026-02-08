package dev.doctor4t.trainmurdermystery.mod_whitelist.server.network;

import dev.doctor4t.trainmurdermystery.mod_whitelist.common.utils.MWLogger;
import net.minecraft.server.level.ServerPlayer;

import java.net.*;
import java.util.*;

/**
 * Utility class for retrieving player network information
 * Includes IP address and MAC address detection
 */
public class PlayerNetworkInfoUtil {

	/**
	 * Gets the IP address of a player
	 *
	 * @param player the server player
	 * @return the player's IP address or "unknown"
	 */
	public static String getPlayerIP(ServerPlayer player) {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "无法获取IP地址: " + e.getMessage();
		}
	}

	/**
	 * Gets the physical address (MAC address) of the player's network interface
	 *
	 * @param player the server player
	 * @return the MAC address as a string or "unknown" if not available
	 */
	public static String getPlayerMACAddress(ServerPlayer player) {
			try {
				// 获取本地主机和网络接口
				InetAddress ip = InetAddress.getLocalHost();
				NetworkInterface network = NetworkInterface.getByInetAddress(ip);

				// 获取MAC地址
				byte[] mac = network.getHardwareAddress();

				// 将MAC地址转换为十六进制字符串
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < mac.length; i++) {
					sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
				}

				return sb.toString();
			} catch (UnknownHostException | SocketException | NullPointerException e) {
				return "无法获取MAC地址: " + e.getMessage();
			}

	}

	/**
	 * Attempts to find any MAC address from available network interfaces
	 *
	 * @return MAC address string or "unknown"
	 */
	private static String tryFindMACAddress() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				
				// Skip loopback and virtual interfaces
				if (networkInterface.isLoopback() || networkInterface.isVirtual()) {
					continue;
				}
				
				byte[] macBytes = networkInterface.getHardwareAddress();
				if (macBytes != null && macBytes.length > 0) {
					return bytesToMACAddress(macBytes);
				}
			}
		} catch (Exception e) {
			MWLogger.LOGGER.debug("Failed to find MAC address from network interfaces", e);
		}
		return "unknown";
	}

	/**
	 * Converts MAC address bytes to hex string format
	 *
	 * @param macBytes the MAC address bytes
	 * @return MAC address in format XX:XX:XX:XX:XX:XX
	 */
	private static String bytesToMACAddress(byte[] macBytes) {
		StringBuilder macAddress = new StringBuilder();
		for (int i = 0; i < macBytes.length; i++) {
			macAddress.append(String.format("%02X", macBytes[i]));
			if (i < macBytes.length - 1) {
				macAddress.append(":");
			}
		}
		return macAddress.toString();
	}

	/**
	 * Gets both IP and MAC address information for a player
	 *
	 * @param player the server player
	 * @return a map containing "ip" and "mac" entries
	 */
	public static Map<String, String> getPlayerNetworkInfo(ServerPlayer player) {
		Map<String, String> info = new HashMap<>();
		info.put("ip", getPlayerIP(player));
		info.put("mac", getPlayerMACAddress(player));
		return info;
	}
}
