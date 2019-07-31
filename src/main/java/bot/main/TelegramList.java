package bot.main;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * List of all telegram groups
 * 
 * @author schieljn
 *
 */
public enum TelegramList {
    SPIELE, FIUSINTERN, ALLGEMEIN, SIFF, ROOT;

	/**
	 * 
	 * To create the /sub[group] and /unsub[group] string for comparison
	 * 
	 * @param appending "sub" or "unsub"
	 * @return A list of strings containing all group names with prefix /[appending]
	 */
    public static ArrayList<String> getAllStrings(String appending) {
        ArrayList<String> output = new ArrayList<String>();
        for (TelegramList temp : TelegramList.values()) {
            output.add("/" + appending + temp);
        }

        return output;
    }

    /**
     * Contains the text a user can get from typing /info
     */
    public static final HashMap<TelegramList, String> groupInfo = new HashMap<TelegramList, String>();
}
