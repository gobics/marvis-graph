/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.keggapi;

import java.io.*;
import java.net.MalformedURLException;
import java.util.logging.Logger;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public abstract class KeggAPICached extends KeggAPI {

	protected static final Logger logger = Logger.getLogger(KeggAPICached.class.
			getName());
	private int number_of_days = 28;

	protected abstract String getCached(String url);

	protected abstract void storeCached(String url, String result);

	public abstract void removeFromCache(String url);

	public void setCacheTime(int days) {
		if (days < 1) {
			throw new IllegalArgumentException("Number of days must be greater than 0");
		}
		number_of_days = days;
	}

	public int getCacheTime() {
		return number_of_days;
	}

	@Override
	protected BufferedReader getReader(String[] arguments) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder(25);
		for (String s : arguments) {
			sb.append("/").append(s);
		}
		String url = sb.toString();

		String result = getCached(url);
		if (result == null) {
			BufferedReader in = super.getReader(arguments);
			sb = new StringBuilder(1000);
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line).append("\n");
			}

			result = sb.toString();
			storeCached(url, result);
		}


		InputStream is = new ByteArrayInputStream(result.getBytes());
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		return in;
	}
}
