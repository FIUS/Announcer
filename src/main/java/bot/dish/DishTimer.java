package bot.dish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;

import bot.main.MessageSender;
import bot.user.UserManager;

/**
 *
 * Manages the timer tasks for all dishwasher
 *
 * @author schieljn
 */
public class DishTimer {

	private Timer[] timer;

	private int[] washer;

	private final MessageSender sender;
	private final UserManager users;

	private static final int WASHING_TIME = 120;
	private static final int REMEMBER_TIME = 60;

	public DishTimer(MessageSender sender, UserManager users) {

		
		washer = new int[8];
		this.sender = sender;
		this.users = users;
		this.timer = new Timer[8];
	}

	/**
	 * 
	 * Sets the state of a specific washer to finished
	 * 
	 * @param washer The index of the washer
	 */
	public void setWasherStateReady(int washer) {
		this.washer[washer] = 2;
	}

	/**
	 * 
	 * Turns the timer of a dishwasher on if the timer is stopped, else turns it off
	 * 
	 * @param dishwasher The name of the dishwasher
	 * @param number     The index of the dishwasher
	 * @return "Geschirrreinigungsapparat wurde geleert" if timer was stopped and
	 *         "Geschirrreinigungsapparat wurde beladen" if timer was started
	 */
	public String toggleTimer(String dishwasher, int number) {
		String output;
		if (washer[number] != 0) {
			timer[number].cancel();
			timer[number].purge();

			output = "Geschirrreinigungsapparat wurde geleert";
			washer[number] = 0;
		} else {
			output = "Geschirrreinigungsapparat wurde beladen";
			timer[number] = new Timer(dishwasher, true);
			timer[number].schedule(new DishTimerTask(dishwasher, sender, users, 2 + "" + number, number, this),
					1000 * 60 * WASHING_TIME, 1000 * 60 * REMEMBER_TIME);

			washer[number] = 1;
		}

		sendDishWaserRequest("changeWasherState=" + washer[number] + "" + number);
		return output;
	}

	/**
	 * 
	 * Sends a state of a specific dishwasher to the dishwasher display
	 * 
	 * @param request Specifies the state and the dishwasher.
	 * Syntax: [state][index]
	 * Replace [state] with 0 for ready, 1 for active and 2 for finished
	 * Replace [index] with the index of the dishwasher
	 * 
	 */
	public static void sendDishWaserRequest(String request) {
		String url = "http://led-dishwasher?" + request;

		HttpURLConnection con = null;
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
			// System.out.println(content.toString());

		} catch (IOException e1) {

			e1.printStackTrace();
		} finally {

			con.disconnect();
		}

	}
}
