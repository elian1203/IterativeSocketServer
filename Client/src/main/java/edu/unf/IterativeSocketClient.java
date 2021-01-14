package edu.unf;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class IterativeSocketClient {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Please enter the server IP address:");
		String host = scanner.nextLine();

		// regex found online, matches any ipv4 address
		// if found to cause issues during the testing stage I will just remove this portion
		if (!host.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}?$")) {
			System.out.println("Invalid IP address specified!");
			return;
		}

		System.out.println("Please enter the port to communicate on:");
		String portInput =  scanner.nextLine();

		if (!isInt(portInput)) {
			System.out.println("Invalid port specified!");
			return;
		}

		int port = Integer.parseInt(portInput);

		System.out.println("Please select the following command to run:");
		System.out.println("(1) Date\t(2) Uptime\t(3) Memory\t(4) Netstat\t(5) Users\t(6) Processes");

		String selectionInput = scanner.nextLine();

		if (!isInt(selectionInput)) {
			System.out.println("Invalid selection!");
			return;
		}

		int selection = Integer.parseInt(selectionInput);
		String command;

		if (selection == 1)
			command = "date";
		else if (selection == 2)
			command = "uptime";
		else if (selection == 3)
			command = "memory";
		else if (selection == 4)
			command = "netstat";
		else if (selection == 5)
			command = "users";
		else if (selection == 6)
			command = "proc";
		else {
			System.out.println("Invalid selection!");
			return;
		}

		try {
			Socket socket = new Socket(host, port);

			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			writer.write(command);
		} catch (IOException e) {
			System.out.println("Error connecting to host! Ensure you have entered the proper host and port numbers.");
			return;
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
