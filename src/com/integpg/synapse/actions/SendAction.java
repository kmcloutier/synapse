package com.integpg.synapse.actions;

import com.integpg.synapse.Synapse;
import com.integpg.synapse.devices.Device;
import java.io.ByteArrayOutputStream;



public class SendAction extends Action {

    private String _deviceName;
    private byte[] _dataBytes;



    public SendAction(String deviceName, String dataString) {
        _deviceName = deviceName;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < dataString.length(); i++) {
            char c = dataString.charAt(i);
            System.out.print(c +" ");
 
            if (c == '\\') {
                System.out.print("escape found " );
                
                c = dataString.charAt(i + 1);
                switch (c) {
                    case '\\':
                        System.out.println("slash found " );
                        continue;
                    case 'r':
                        System.out.print("carrage return found " );
                        c = '\r';
                        i++;
                        break;
                    case 'n':
                        System.out.print("line feed found " );
                        c = '\n';
                        i++;
                        break;
                }
            }

            System.out.println(c);
            baos.write(c);
        }
        _dataBytes = baos.toByteArray();
        System.out.println(new String(_dataBytes));
    }



    public void execute() throws Exception {
        try {
            // get device
            Device red = Synapse.INSTANCE.getDevice(_deviceName);


            // trigger send on device
            red.send(_dataBytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
