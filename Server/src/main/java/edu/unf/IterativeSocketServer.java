package edu.unf;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class IterativeSocketServer {

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
			System.out.println("Waiting for client");
			// accept next client
			Socket socket = serverSocket.accept();
			System.out.println("Got client");

			// read their command

			DataInputStream dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			String inputCommand = dataIn.readLine();

			System.out.println("Received command: " + inputCommand);

			// initial runtime and process for executing shell command
			Runtime rt = Runtime.getRuntime();
			Process proc;

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
			Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			// each line that is outputted from the process will be sent back to the client
			String procInput;
			while ((procInput = stdInput.readLine()) != null) {
				out.append(procInput + "\n");
			}

			// clean up streams and socket before accepting next client
			stdInput.close();
			out.flush();
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
