package edu.unf;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class IterativeSocketServer {

	public static void main(String[] args) {
		try {
			int port = requestPort();

			if (port == -1)
				return;

			ServerSocket serverSocket = new ServerSocket(port);

			System.out.printf("Started socket server on %s:%d\n", serverSocket.getInetAddress(), port);
			receiveConnections(serverSocket);
		} catch (IOException e) {
			System.out.println("Error initializing server socket! Something else may be running on this port or you " +
					"may have insufficient privileges to listen on this port");
			return;
		}

	}

	private static int requestPort() {
		System.out.println("Please enter a port to listen on:");

		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		scanner.close();

		// a port must be an integer
		if (!isInt(input)) {
			System.out.println("Invalid port specified!");
			return -1;
		}

		int port = Integer.parseInt(input);

		// you cannot have a negative port
		if (port < 0) {
			System.out.println("Invalid port specified!");
			return -1;
		}

		// return specified port
		return port;
	}

	private static void receiveConnections(ServerSocket serverSocket) throws IOException {
		// loop to indefinitely accept clients until closed by SIGTERM or something else
		while (true) {
			// accept next client
			Socket socket = serverSocket.accept();

			// read their command
			Scanner scanner = new Scanner(socket.getInputStream());
			String inputCommand = scanner.next();

			Runtime rt = Runtime.getRuntime();
			Process proc = null;

			// execute process based on inputted command
			switch (inputCommand) {
				case "date":
					proc = rt.exec("date");
					break;
				case "uptime":
					proc = rt.exec("uptime");
					break;
				case "memory":
					proc = rt.exec("free");
					break;
				case "netstat":
					proc = rt.exec("netstat");
					break;
				case "users":
					proc = rt.exec("users");
					break;
				case "proc":
					proc = rt.exec("ps aux");
					break;
				default: // this should never execute unless the socket is breached by a foreign client
					System.out.println("Unknown command specified: " + inputCommand);
					socket.close();
					return;
			}

			// prepare input reader from process and output stream from socket
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream());

			// each line that is outputted from the process will be sent back to the client
			String procInput;
			while ((procInput = stdInput.readLine()) != null) {
				writer.write(procInput + "\n");
			}

			socket.close();
		}
	}

	private static boolean isInt(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
