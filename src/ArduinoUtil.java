import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoUtil {

    public static String addArduino() {
        System.out.print("Connect your Arduino and enter any key when it has been connected: ");
        Scanner s = new Scanner(System.in);
        s.nextLine();
        SerialPort ports[] = SerialPort.getCommPorts();
        if (ports.length == 1) {
            System.out.println("Unable to detect an connected Arduino. Returning to main menu.");
            try {Thread.sleep(2000); } catch (InterruptedException e) {e.printStackTrace();}
            return "";
        }
        System.out.println("Detected devices: ");
        for (int i = 1; i < ports.length; i++) {
            System.out.println(i + ". " + ports[i].getDescriptivePortName() + " connected as " + ports[i].getSystemPortName());
        }

        System.out.print("\nEnter the number of the device you would like to select: ");
        int selection = s.nextInt();
        s.nextLine();
        SerialPort port = ports[selection];
        String serial = getSerialNumber(port);
        if (serial == null) {
            System.out.println("\nThis board is not compatible. Returning to main menu...");
            try {Thread.sleep(2000); } catch (InterruptedException e) {e.printStackTrace();}
            return "";
        }

        return serial;
        
    }

    private static String getSerialNumber(SerialPort port) {
        String serial = null;
        try {
            Process p = Runtime.getRuntime().exec("bash getSerial.sh " + port.getSystemPortName());
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            serial = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serial;
    }
    
    public static Boolean checkArduinoConnection(String authenticSerial, byte[] salt) {
        System.out.println("Please connect the arduino associated with this account.");
        System.out.println("Press any key when you have connected it");
        Scanner s = new Scanner(System.in);
        s.nextLine();

        // Not sure how this will work if there are multiple comm ports/multiple arduinos connected. Can solve this with the authentication
        SerialPort port = null;
        SerialPort ports[] = SerialPort.getCommPorts();
        for (int i = 1; i < ports.length; i++) {
            if (HashUtil.hashPassword(salt, getSerialNumber(ports[i]).toCharArray()).equals(authenticSerial)) {
                port = ports[i];
                break;
            }
        }

        if (port == null) {
            System.out.println("Could not find authenticated Arduino. Ensure that you have connected the same Arduino you used in account creation.");
            System.out.println("Returning to main screen");
            try {Thread.sleep(3000); } catch (InterruptedException e) {e.printStackTrace();}
            return false;
        }
        
        if (port.openPort()) {
            System.out.println("Successfully opened the port");
        } else {
            System.out.println("Unable to open the port. Returning to main screen");
            try {Thread.sleep(2000); } catch (InterruptedException e) {e.printStackTrace();}
            return false;
        }

        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        Scanner data = new Scanner(port.getInputStream());
        if (data.hasNext()){
            if (data.nextInt() == 1) {
                data.close();
                System.out.println("Verification complete");
                return true;
            }
        }

        data.close();
        return false;
    }

}
