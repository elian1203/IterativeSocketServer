package edu.unf;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class IterativeSocketClient {

	public static void main(String[] args) throws InterruptedException {
		// load inputs from stdin
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

		// parse selection to string

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

		// load more inputs from stdin/user

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

		// thread array to store each created process
		List<Thread> threads = new ArrayList<>();

		// atomic to allow modification within thread
		AtomicBoolean error = new AtomicBoolean(false);

		// log start time before creating threads
		long totalStartTime = System.currentTimeMillis();

		// for loop to create each thread
		for (int i = 0; i < numberToExecute; i++) {
			// if there was an error in the last process, we will no longer try to create more
			if (error.get())
				break;

			// final increment to display thread number to stdout
			final int finalI = i + 1;

			Thread t = new Thread(() -> {
				System.out.println("Thread " + finalI + ": Connecting..");

				try {
					// log start time
					long threadStartTime = System.currentTimeMillis();

					// open socket
					Socket socket = new Socket(host, port);
					System.out.println("Thread " + finalI + ": Socket opened");

					// write command to server
					Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					out.append(command).append("\n").flush();

					// read server output
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					String socketRead;
					while ((socketRead = reader.readLine()) != null) {
						// print line by line to stdout
						System.out.println(socketRead);
					}

					// calculate the final time or turn-around time
					long threadFinalTime = System.currentTimeMillis() - threadStartTime;

					System.out.println("Thread " + finalI + ": Connection closed. Turn-around time: " + threadFinalTime + "ms.");
				} catch (IOException e) {
					// only do this if there hasn't already been an error determined
					if (!error.get()) {
						System.out.println("Error connecting to host! " +
								"Ensure you have entered the proper host and port numbers.");
						// if there was an error, set the atomicboolean to true to cease execution
						error.set(true);
					}
				}
			});

			// start thread and add it to list
			t.start();
			threads.add(t);
		}

		// await for every thread to finish for calculating total time
		for (Thread thread : threads)
			thread.join();

		// total time = current time - start time
		long totalFinalTime = System.currentTimeMillis() - totalStartTime;

		System.out.println("Total turn-around time: " + totalFinalTime + "ms.");
		System.out.println("Average turn-around time: " + totalFinalTime / numberToExecute + "ms.");
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
