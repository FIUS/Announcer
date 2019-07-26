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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.TimerTask;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 *
 * @author Justin
 */
public class DishTimerTask extends TimerTask {

	private String dishwasher;
	private MessageSender sender;
	private UserManager users;
	private DishTimer timer;
	private String washerHTTPOut;
private int washerNumber;

	public DishTimerTask(String dishwasher, MessageSender sender, UserManager users, String washerOut,
			int washerNumber, DishTimer timer) {
		this.dishwasher = dishwasher.toLowerCase();
		this.sender = sender;
		this.users = users;
		this.washerHTTPOut = washerOut;
		this.timer = timer;
		this.washerNumber=washerNumber;
	}

	@Override
	public void run() {

		if (!sendDoorRequest()) {
			return;
		}

		timer.setWasherStateReady(washerNumber);
		DishTimer.sendDishWaserRequest("changeWasherState=" + washerHTTPOut);

		HashSet<User> dishUser = users.usersOnList(TelegramList.SIFF);

		StringBuilder messageToSend = new StringBuilder();

		messageToSend.append("Entleert den Geschirrreinigungsapparat _");
		messageToSend.append("\"");
		String temp = dishwasher.substring(0, 1).toUpperCase() + dishwasher.substring(1);
		messageToSend.append(temp);
		messageToSend.append("\"");
		messageToSend.append("_!");

		String alert = messageToSend.toString();
		String pic = "";

		switch (dishwasher) {
		case "asterix":
			pic = "\u2612\u2610\u2610\u2610\n\u2610\u2610\u2610\u2610";
			break;
		case "obelix":
			pic = "\u2610\u2612\u2610\u2610\n\u2610\u2610\u2610\u2610";
			break;
		case "idefix":
			pic = "\u2610\u2610\u2612\u2610\n\u2610\u2610\u2610\u2610";
			break;
		case "miraculix":
			pic = "\u2610\u2610\u2610\u2612\n\u2610\u2610\u2610\u2610";
			break;
		case "tick":
			pic = "\u2610\u2610\u2610\u2610\n\u2612\u2610\u2610\u2610";
			break;
		case "trick":
			pic = "\u2610\u2610\u2610\u2610\n\u2610\u2612\u2610\u2610";
			break;
		case "track":
			pic = "\u2610\u2610\u2610\u2610\n\u2610\u2610\u2612\u2610";
			break;
		case "donald":
			pic = "\u2610\u2610\u2610\u2610\n\u2610\u2610\u2610\u2612";
			break;
		default:
			pic = "Dieser Geschirrreinigungsapparat wurde nicht vom Amt zugelassen";
			break;
		}

		for (User u : dishUser) {
			sender.sendMessage(alert, u.id);
			sender.sendMessage(pic, u.id);
		}

	}

	private boolean sendDoorRequest() {
		URL url;
		String out = "";
		try {

			String a = "https://fius.informatik.uni-stuttgart.de/isOpen.php";
			url = new URL(a);
			URLConnection conn = url.openConnection();

			try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String inputLine;
				while ((inputLine = br.readLine()) != null) {
					out += inputLine;
				}
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.equals("open");
	}

}
