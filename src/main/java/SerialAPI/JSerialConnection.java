package SerialAPI;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.*;

public class JSerialConnection {

    private final int BAUD_RATE = 19200;
    private final int DATA_BITS = 8;
    private final int STOP_BITS = 1;
    private final int PARITY = 0;

    private final SerialPort sp;

    public JSerialConnection(String com){
        sp = SerialPort.getCommPort(com);
        sp.setComPortParameters(BAUD_RATE,DATA_BITS,STOP_BITS,PARITY);
        sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING,0,0);

    }

    public void sendStation(String station){
        if(!sp.openPort()){
            //TODO Add failed connection exception.
            return;
        }

        sp.addDataListener(new PortListener());

        try(DataOutputStream os = new DataOutputStream(sp.getOutputStream())){
            System.out.println("Writing " + station);
            os.writeUTF(station + '\n');



        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private final class PortListener implements SerialPortDataListener {

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            System.out.println("Data Received");

            byte[] newData = serialPortEvent.getReceivedData();

            if(newData.length > 2) {
                System.out.println("Received data of size: " + newData.length);
                //Start on 1 since the first response is an Acknowledgement
                for (int i = 1; i < newData.length; ++i) {
                    System.out.print((char) newData[i]);
                }
                //Close port after response received
                serialPortEvent.getSerialPort().closePort();
            }

        }
    }


}
