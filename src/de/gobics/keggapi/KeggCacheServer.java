package de.gobics.keggapi;

import de.gobics.marvis.utils.LoggingUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple webserver listening for request and querying the cache database for
 * result. This is used to wrap requests from other tools (e.g. Perl Scripts).
 *
 * @author manuel
 */
public class KeggCacheServer {

	private static final int PORT = 8080;
	private static final Logger logger = Logger.getLogger(KeggCacheServer.class.getName());
	private static final ExecutorService wokerpool = Executors.newFixedThreadPool(10);

	public static void main(String[] args) throws IOException {
		LoggingUtils.initLogger(Level.INFO);
		ServerSocket server = new ServerSocket(PORT);

		logger.info("Wait for incoming connections at: *:" + PORT);
		while (true) {
			try {
				Socket socket = server.accept();
				if (socket != null) {
					wokerpool.submit(new Request(socket));
				}
			}
			catch (Throwable e) {
				logger.log(Level.SEVERE, "Got timeout: " + e.getMessage());
			}
		}
	}

	private static class Request implements Runnable {

		private final Socket socket;

		public Request(Socket socket) {
			this.socket = socket;
		}

		private void response(String content) throws IOException {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			// Write the header
			out.write("HTTP/1.1 ");
			out.write(content != null ? "200 Ok" : "404 Not Found");
			out.write("\r\n");
			// Content type
			out.write("Content-Type: text/plain\r\n");

			// Finish header
			out.write("\r\n");

			if (content != null) {
				out.write(content.replaceAll("\r?\n", "\n"));
			}

			out.flush();
		}

		private String getPath() throws Exception {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = in.readLine();
			if (!line.startsWith("GET ")) {
				throw new Exception("Malformed Request does not start with GET");
			}

			line = line.substring(4); // remove the get


			int idx = line.indexOf("HTTP/1.1");
			if (idx < 0) {
				throw new Exception("Can not find end");
			}

			line = line.substring(0, idx);

			if (line.startsWith("/")) {
				line = line.replaceFirst("/", "");
			}

			return line;
		}

		@Override
		public void run() {
			try {
				dorun();
			}
			catch (Throwable e) {
				logger.log(Level.SEVERE, "Uncaught error in thread: {0}", e);
			}

		}

		private void dorun() {
			String path = null;
			try {
				path = getPath();
			}
			catch (Throwable e) {
				logger.log(Level.SEVERE, "Can not get path: {0}", e);
			}
			if (path == null) {
				return;
			}

			logger.log(Level.FINE, "Serving: {0}", path);

			KeggAPI api = new KeggAPI();
			try {
				KeggMysqlCache capi = new KeggMysqlCache("biofung.gobics.de", "keggcache", "keggcache", "keggcache");
				if (capi.tryConnect()) {
					logger.finer("Using cached API");
					api = capi;
				}
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "Can not connect to MySQL cache: {0}", e);
			}

			String result = null;
			try {
				result = api.fetch(path);
			}
			catch (MalformedURLException ex) {
				logger.log(Level.SEVERE, "Malformed url {0}: {1} ", new Object[]{path, ex});
			}
			catch (IOException ex) {
				logger.log(Level.SEVERE, "Can not fetch url {0}: {1}", new Object[]{path, ex});
			}
			try {
				response(result);
			}
			catch (IOException ex) {
				logger.log(Level.SEVERE, "Can not send response: ", ex);
			}

			// Close the socket
			try {
				if (!socket.isClosed()) {
					socket.close();
				}
			}
			catch (IOException ex) {
				logger.log(Level.SEVERE, "Can not close socket: ", ex);
			}

			// Clean up
			if (api instanceof KeggMysqlCache) {
				((KeggMysqlCache) api).disconnect();
			}
		}
	}
}
