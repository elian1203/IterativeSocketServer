package edu.unf;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class IterativeSocketClient {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Please enter the server IP address:");
		String host = scanner.nextLine();

		System.out.println("Please enter the port to communicate on:");
		String portInput = scanner.nextLine();

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

		System.out.println("How many times would you like to run this command on the server? (1-25)");

		String numberInput = scanner.nextLine();

		if (!isInt(numberInput)) {
			System.out.println("Invalid number of times to execute");
			return;
		}

		int numberToExecute = Integer.parseInt(numberInput);

		if (numberToExecute < 1 || numberToExecute > 25) {
			System.out.println("Invalid number of times to execute! Selection must be between 1 and 25.");
			return;
		}

		long totalTime = 0;

		for (int i = 0; i < numberToExecute; i++) {
			System.out.println("Connecting..");

			try {
				long startTime = System.currentTimeMillis();
				Socket socket = new Socket(host, port);
				System.out.println("Socket opened");

				// Use encoding of your choice
				Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				out.append(command).append("\n").flush();

				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String socketRead;
				while ((socketRead = reader.readLine()) != null) {
					System.out.println(socketRead);
				}

				long finalTime = System.currentTimeMillis() - startTime;
				totalTime += finalTime;

				System.out.println("Connection closed. Turn-around time: " + finalTime + "ms.");
			} catch (IOException e) {
				System.out.println("Error connecting to host! " +
						"Ensure you have entered the proper host and port numbers.");
				return;
			}
		}

		System.out.println("Total turn-around time: " + totalTime + "ms.");
		System.out.println("Average turn-around time: " + totalTime / numberToExecute + "ms.");
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
