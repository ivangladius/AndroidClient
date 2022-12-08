import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

	private Socket clientSocket;
	private InetAddress address;
	private Integer port;

	private ExecutorService executor;

	// private PrintWriter output;
	// private BufferedReader input;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public Client(String address, Integer port) {
		try {
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			System.out.println("HOST NOT REACHABLE !!!");
			e.printStackTrace();
		}
		this.port = port;
	}

	private void connect() {
		try {
			clientSocket = new Socket(this.address, this.port);
			// output = new PrintWriter(clientSocket.getOutputStream(), true);
			out = new ObjectOutputStream(clientSocket.getOutputStream());

		} catch (IOException e) {
			System.out.println("COULD NOT CONNECT!!!");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void closeSocket() {
		if (!clientSocket.isClosed()) {
			try {
				clientSocket.close();
			} catch (IOException ignore) {
			}
		}
	}

	public String requestData(String operation) {

		executor = Executors.newSingleThreadExecutor();
		Future<JSONObject> jsonFuture;

		jsonFuture = executor.submit(() -> {

			connect();

			PrintWriter out;
			BufferedReader in;

			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			JSONObject jsonMessage = new JSONObject();
			jsonMessage.put("key", "403");
			jsonMessage.put("operation", operation);
			jsonMessage.put("payload", " ");

			out.println(jsonMessage.toString());
			String replyString = in.readLine();

			int i = replyString.indexOf("{");
			replyString = replyString.substring(i);
			JSONObject json = new JSONObject(replyString.trim());
			System.out.println(json.toString(4));

			return json;
		});

		JSONObject data = null;
		try {
			data = jsonFuture.get(5L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		executor.shutdown();

		closeSocket();

		if (data != null) {
			try {
				return data.get("payload").toString();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
