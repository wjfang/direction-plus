package org.silentsquare.dplus.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class CronTest {
	private static String url = "http://localhost:8080/updateNewsDB?key=hellopiggy";
	private static int period = 60000; // in ms

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Timer timer = new Timer("Cron Task");
		timer.scheduleAtFixedRate(
				new TimerTask() {
					@Override
					public void run() {
						try {
							new URL(url).openConnection().getInputStream();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.out.println("Visited " + url);
					}
				}, 0, period);
	}

}
