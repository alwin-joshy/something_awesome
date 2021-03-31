import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.fazecast.jSerialComm.SerialPort;


// This is a really disgustingly big class but only because I have to handle all the possible outputs from the enroll and fingerprint arduino methods 
// in here rather than having them be handled on the Arduino :(

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

        System.out.println("Arduino connection successful!");

        return serial;
        
    }

    public static int getFingerprint(String authenticSerial, byte[] salt) {
        SerialPort p = checkArduinoConnection(authenticSerial, salt);
        if (p == null) {
            return -1;
        }
        if (! p.openPort()) {
            System.out.println("Unable to open the port. Returning to main screen");
            try {Thread.sleep(2000); } catch (InterruptedException e) {e.printStackTrace();}
            return -1;
        }
        uploadSketch(p, "fingerprint/");
        p.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        Scanner dataIn = new Scanner(p.getInputStream());

        try {

            System.out.println("Place finger on sensor");
        
            while (dataIn.hasNext()) {
                String s = dataIn.next();
                Boolean check = false;
                switch (s) {
                    case "taken":
                        check = true;
                        break;
                    case "communication":
                        System.out.println("Communication error");
                        break;
                    case "imaging":
                        System.out.println("Imaging error");
                        break;
                    case "unknown":
                        System.out.println("Imaging error");
                        break;
                }
                if (check) break;
            }

            String s = dataIn.next();
            switch (s) {
                case "converted":
                    break;
                case "messy":
                    System.out.println("Image too messy");
                    return -1;
                case "communication":
                    System.out.println("Communication error");
                    return -1;
                case "features":
                    System.out.println("Could not find fingerprint features");
                    return -1;
                case "unknown":
                    System.out.println("Unknown error");
                    return -1;
            }

            s = dataIn.next();
            switch (s) {
                case "match":
                    break;
                case "noMatch":
                    System.out.println("Could not find a matching fingerprint");
                    return 0;
                case "communication":
                    System.out.println("Communication error");
                    return -1;
                case "unknown":
                    System.out.println("Unknown error");
                    return -1;
            }

            s = dataIn.next();
            System.out.println(s);

            return Integer.parseInt(s); 

        } finally {
            dataIn.close();
            p.closePort();
        }

        

    } 

    public static int addFingerprint(String authenticSerial, byte[] salt) throws IOException {
        SerialPort p = checkArduinoConnection(authenticSerial, salt);
        if (p == null) {
            return 0;
        }
        if (! p.openPort()) {
            System.out.println("Unable to open the port. Returning to main screen");
            try {Thread.sleep(2000); } catch (InterruptedException e) {e.printStackTrace();}
            return 0;
        }
        uploadSketch(p, "enroll/");
        int fID = SqliteDB.getCurrentFingerprint();

        p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        
        Scanner dataIn = new Scanner(p.getInputStream());


        try {
            OutputStream dataOut = p.getOutputStream();

            dataOut.write((byte) fID);
            dataOut.flush();

            while (dataIn.hasNext()){
                String s = dataIn.next();
                boolean check = false;
                switch (s) {
                    case "taken":
                        check = true;
                        break;
                    case "communication":
                        System.out.println("Communication error");
                        break;
                    case "imaging":
                        System.out.println("Imaging error");
                        break;
                    case "unknown":
                        System.out.println("Unknown error");
                        break;
                }
                if (check) break;   
            }

            
            String s = dataIn.next();
            switch (s) {
                case "converted":
                    break;
                case "messy":
                    System.out.println("Image too messy");
                    return 0;
                case "communication":
                    System.out.println("Communication error");
                    return 0;
                case "features":
                    System.out.println("Could not find fingerprint features");
                    return 0;
                case "unknown":
                    System.out.println("Unknown error");
                    return 0;
            }
            

            System.out.println("Remove finger");
            try {Thread.sleep(2000); } catch (InterruptedException e) {e.printStackTrace();}

            while (dataIn.hasNext()){
                s = dataIn.next();
                boolean check = false;
                switch (s) {
                    case "noFinger":
                        check = true;
                        break;
                    case "finger":
                        break;
                }
                if (check) break;   
            }

            System.out.println("Place same finger again");

            while (dataIn.hasNext()){
                s = dataIn.next();
                boolean check = false;
                switch (s) {
                    case "taken":
                        check = true;
                        break;
                    case "communication":
                        System.out.println("Communication error");
                        break;
                    case "imaging":
                        System.out.println("Imaging error");
                        break;
                    case "unknown":
                        System.out.println("Unknown error");
                        break;
                }
                if (check) break;   
            }

            
            s = dataIn.next();
            switch (s) {
                case "converted":
                    break;
                case "messy":
                    System.out.println("Image too messy");
                    return 0;
                case "communication":
                    System.out.println("Communication error");
                    return 0;
                case "features":
                    System.out.println("Could not find fingerprint features");
                    return 0;
                case "unknown":
                    System.out.println("Unknown error");
                    return 0;
            }

            s = dataIn.next();
            switch (s) {
                case "matched":
                    System.out.println("Prints matched!");
                    break;
                case "communication":
                    System.out.println("Communication error");
                    return 0;
                case "nomatch":
                    System.out.println("Fingerprints did not match");
                    return 0;
                case "unknown":
                    System.out.println("Unknown error");
                    return 0;
            }


            s = dataIn.next();
            switch (s) {
                case "stored":
                    System.out.println("Success!");
                    break;
                case "communication":
                    System.out.println("Communication error");
                    return 0;
                case "location":
                    System.out.println("Could not store in that location");
                    return 0;
                case "flash":
                    System.out.println("Error writing to flash");
                    return 0;
                case "unknown":
                    System.out.println("Unknown error");
                    return 0;
            }

            SqliteDB.updateCurrentFingerPrint();
            return fID;
        } finally {
            dataIn.close();
            p.closePort();
        }
        
    }

    private static void uploadSketch(SerialPort port, String sketchDir) {
        try {
            //System.out.println("bash uploadSketch.sh " + sketchDir + " " + port.getSystemPortName());
            Process p = Runtime.getRuntime().exec("bash uploadSketch.sh " + sketchDir + " " + port.getSystemPortName());
            // This is really only necessary for debugging - wtf???? it doesnt work without this????????? 
            // HUH??!?!? Apparently output must be consumed for it to work properly.
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            while (reader.readLine() != null) {
                //System.out.println(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    
    public static SerialPort checkArduinoConnection(String authenticSerial, byte[] salt) {
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
            return null;
        }
        
        if (port.openPort()) {
            System.out.println("Successfully opened the port");
            try {Thread.sleep(1000); } catch (InterruptedException e) {e.printStackTrace();}
        } else {
            System.out.println("Unable to open the port. Returning to main screen");
            try {Thread.sleep(1000); } catch (InterruptedException e) {e.printStackTrace();}
            return null;
        }

        port.closePort();
        return port;
    }

}
