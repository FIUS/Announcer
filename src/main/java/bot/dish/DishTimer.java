/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot.dish;

import bot.main.MessageSender;
import bot.main.TelegramList;
import bot.user.User;
import bot.user.UserManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Timer;

/**
 *
 * @author Justin
 */
public class DishTimer {

	private Timer[] timer;
	private boolean isActive;

	private int[] washer;

	private final MessageSender sender;
	private final UserManager users;

	private static final int WASHING_TIME = 120;
	private static final int REMEMBER_TIME = 60;

	public DishTimer(MessageSender sender, UserManager users) {

		isActive = false;
		washer = new int[8];
		this.sender = sender;
		this.users = users;
		this.timer = new Timer[8];
	}

	public void setWasherStateReady(int state) {
		washer[state] = 2;
	}

	public String toggleTimer(String dishwasher, int number) {
		String output;
		if (washer[number] != 0) {
			timer[number].cancel();
			timer[number].purge();
			
			output="Geschirrreinigungsapparat wurde geleert";
			washer[number] = 0;
		} else {
			output="Geschirrreinigungsapparat wurde beladen";
			timer[number] = new Timer(dishwasher, true);
			timer[number].schedule(new DishTimerTask(dishwasher, sender, users, 2 + "" + number, number, this),
					1000 * 60 * WASHING_TIME, 1000 * 60 * REMEMBER_TIME);
					
			washer[number] = 1;
		}

		sendDishWaserRequest("changeWasherState=" + washer[number] + "" + number);
		return output;
	}

	public static void sendDishWaserRequest(String request) {
		String url = "http://led-dishwasher?"+request;
			
		HttpURLConnection con=null;
		try {
			
			URL myurl = new URL(url);
			con = (HttpURLConnection) myurl.openConnection();

			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Java client");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			StringBuilder content;
			content = new StringBuilder();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				String line;
				while ((line = in.readLine()) != null) {
					content.append(line);
					content.append(System.lineSeparator());
				}
			} catch (IOException e) {
				System.out.println("Connection timed out");
			}
			//System.out.println(content.toString());

		} catch (IOException e1) {
			
			e1.printStackTrace();
		} finally {

			con.disconnect();
		}

	}
}
