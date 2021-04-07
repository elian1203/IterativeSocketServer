package edu.unf;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SocketServer {

	public static void main(String[] args) {
		try {
			// request port from stdin / user
			int port = requestPort();

			// if port is -1, the user specified an invalid port
			if (port == -1)
				return;

			// open server socket
			ServerSocket serverSocket = new ServerSocket(port);

			// get local ip address from socket
			String localIpv4 = Inet4Address.getLocalHost().getHostAddress();
			System.out.printf("Started socket server on %s:%d\n", localIpv4, port);

			// begin receiving connections
			receiveConnections(serverSocket);
		} catch (IOException e) {
			System.out.println("Error initializing server socket! Something else may be running on this port or you " +
					"may have insufficient privileges to listen on this port");
			e.printStackTrace();
		}

	}

	private static int requestPort() {
		System.out.println("Please enter a port to listen on:");

		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		scanner.close();

		// a port must be an integer
		if (notInt(input)) {
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
			System.out.println("Waiting for client");
			// accept next client
			final Socket socket = serverSocket.accept();
			System.out.println("Got client");

			new Thread(() -> {
				// read their command
				try {
					DataInputStream dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
					int inputSelection = dataIn.readInt();

					Runtime rt = Runtime.getRuntime();
					Process proc;

					String commandName;

					// execute process based on inputted command
					if (inputSelection == 1) {
						commandName = "date";
						proc = rt.exec(commandName);
					} else if (inputSelection == 2) {
						commandName = "uptime";
						proc = rt.exec(commandName);
					} else if (inputSelection == 3) {
						commandName = "memory";
						proc = rt.exec("free");
					} else if (inputSelection == 4) {
						commandName = "netstat";
						proc = rt.exec(commandName);
					} else if (inputSelection == 5) {
						commandName = "users";
						proc = rt.exec(commandName);
					} else if (inputSelection == 6) {
						commandName = "processes";
						proc = rt.exec("ps aux");
					} else { // this should never execute unless the socket is breached by a foreign client
						System.out.println("Unknown command specified: " + inputSelection);
						socket.close();
						return;
					}

					System.out.println("Received command: " + commandName);

					// initial runtime and process for executing shell command

					// prepare input reader from process and output stream from socket
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

					// each line that is outputted from the process will be sent back to the client
					String procInput;
					while ((procInput = stdInput.readLine()) != null) {
						out.append(procInput).append("\n");
					}

					// clean up streams and socket before accepting next client
					stdInput.close();
					out.flush();
					socket.close();

				} catch (IOException e) {
					System.out.println("Error handling client!");
				}
			}).start();
		}
	}

	private static boolean notInt(String input) {
		try {
			Integer.parseInt(input);
			return false;
		} catch (NumberFormatException e) {
			return true;
		}
	}
}
