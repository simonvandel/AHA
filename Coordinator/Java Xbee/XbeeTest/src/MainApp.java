/**
 * Copyright (c) 2014-2015 Digi International Inc.,
 * All rights not expressly granted are reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Digi International Inc. 11001 Bren Road East, Minnetonka, MN 55343
 * =======================================================================
 */

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.HexUtils;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;

public class MainApp {

	/* Constants */

	// TODO Replace with the serial port where your receiver module is
	// connected.
	private static String PORT = "COM5";
	// TODO Replace with the baud rate of you receiver module.
	private static final int BAUD_RATE = 9600;

	public static void main(String[] args) {
		XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);

		while (true) {
			// wait 1000 miliseconds for data
			XBeeMessage xBMsg = RecieveData(myDevice);
			// if data recieved succesfully
			if (xBMsg != null) {
				try {
					System.out.format("From %s >> %s | %s%n", xBMsg.getDevice()
							.get64BitAddress(), // source addr
							HexUtils.prettyHexString(HexUtils
									.byteArrayToHexString(xBMsg.getData())), // data
																				// in
																				// hex
							new String(xBMsg.getData())); // data in text
					// init return string
					String toSend = "Ok";
					myDevice.setDestinationAddress(xBMsg.getDevice()
							.get64BitAddress()); // set DL/DH (target addr) to
													// source of msg
					BroadcastSend(toSend.getBytes(), myDevice);
				} catch (XBeeException e) {
					e.printStackTrace();
				} finally {
					if(myDevice.isOpen()) myDevice.close();
					try {
					myDevice.setDestinationAddress(XBee64BitAddress.BROADCAST_ADDRESS);
					} catch(XBeeException e) {}
				}
			}
		}

	}

	public static XBeeMessage RecieveData(XBeeDevice myDevice) {
		System.out.println(" +-----------------------------------------+");
		System.out.println(" |  XBee Java Library Data Polling Sample  |");
		System.out.println(" +-----------------------------------------+\n");

		try {
			myDevice.open();
			XBeeMessage xBeeMsg = myDevice.readData(1000);
			myDevice.close();

			return xBeeMsg;
		} catch (XBeeException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static void TargetSend(byte[] dataToSend, XBeeDevice myDevice) {
		String REMOTE_NODE_IDENTIFIER = "REMOTE"; //??????
		try {
			myDevice.open();

			// Obtain the remote XBee device from the XBee network.
			XBeeNetwork xbeeNetwork = myDevice.getNetwork();
			RemoteXBeeDevice remoteDevice = xbeeNetwork.getDevice(myDevice.getDestinationAddress());
			if (remoteDevice == null) {
				System.out
						.println("Couldn't find the remote XBee device with '"
								+ REMOTE_NODE_IDENTIFIER + "' Node Identifier.");
				System.exit(1);
			}

			System.out.format("Sending data to %s >> %s | %s... ", remoteDevice
					.get64BitAddress(), HexUtils.prettyHexString(HexUtils
					.byteArrayToHexString(dataToSend)), new String(dataToSend));

			myDevice.sendData(remoteDevice, dataToSend);

			System.out.println("Success");

		} catch (XBeeException e) {
			System.out.println("Error");
			e.printStackTrace();
			System.exit(1);
		} finally {
			myDevice.close();
		}

	}

	public static void BroadcastSend(byte[] dataToSend, XBeeDevice myDevice) {
		System.out
				.println(" +------------------------------------------------+");
		System.out
				.println(" |  XBee Java Library Send Broadcast Data Sample  |");
		System.out
				.println(" +------------------------------------------------+\n");

		try {
			myDevice.open();

			System.out.format("Sending broadcast data: '%s'...%n", new String(
					dataToSend));

			myDevice.sendBroadcastData(dataToSend);

			System.out.println("Success");

		} catch (XBeeException e) {
			System.out.println("Error");
			e.printStackTrace();
			System.exit(1);
		} finally {
			myDevice.close();
		}
	}

}
