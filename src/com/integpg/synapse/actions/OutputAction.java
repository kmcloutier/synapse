package com.integpg.synapse.actions;

import com.integpg.system.JANOS;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Json;



public class OutputAction extends Action {

    private String _action;



    public OutputAction(Json json) {
        String action = (String) json.get("Action");
        int channel = Integer.valueOf((String) json.get("Channel")).intValue();


        if (action.equalsIgnoreCase("close pulse")) _action = "cp";

        _action += channel;


        ActionHash.put((String) json.get("ID"), this);
    }



    public void execute() throws IOException {
        parseCommand(_action);
    }



    public static byte[] parseCommand(String lowerCommand) throws IOException {
        if (lowerCommand.indexOf(",") >= 0) {
            int begin = 0;
            while (begin >= 0) {
                int end = lowerCommand.indexOf(",", begin + 1);
                parseCommand(lowerCommand, begin, end);
                if (end >= 0) {
                    end++;
                }
                begin = end;
            }
        } else {
            return parseCommand(lowerCommand, 0, lowerCommand.length());
        }

        return null;
    }



    public static byte[] parseCommand(String lowerCommand, int beginIndex, int endIndex) throws IOException {
        if (endIndex == -1) endIndex = lowerCommand.length();
        lowerCommand = lowerCommand.substring(beginIndex, endIndex);


        //        long start = TINIOS.uptimeMillis();
        // used for caching the action
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        //        System.out.println(command);

        int i, n;
        int states = 0;
        int mask = 0;
//        int res = 0;
//        int setmask = 0;
        int parameter = 1000;
        boolean close = false;
        boolean open = false;
        boolean toggle = false;
        boolean reset = false;
        boolean setcounters = false;
        boolean dosetcounters = false;
        boolean pulse = false;
        boolean value = false;
        boolean stop = false;
//        boolean counterdump = false;
//        boolean usagedump = false;
        int shift = 0;

        int len = lowerCommand.length();
        char[] chars = new char[len];
        lowerCommand.getChars(0, len, chars, 0);

        int currentStates = JANOS.getOutputStates();
//        System.out.println("Current States: " + Integer.toBinaryString(currentStates));
        states = currentStates;

        for (i = 0; i < len && !stop; i++) { /* each character in the comand */

            switch (chars[i]) {
//                    case 'q':   // [Q]uit - exits command
//                        m_quitApp = true;
//                        stop = true;   // ends command interpretation
//                        break;  // will exit

                case 'c':   // [C]lose sets relay state to 1

                    close = true;
                    open = false;
                    toggle = false;
                    reset = false;
                    setcounters = false;
                    break;  // next character

                case 'o':   // [O]pen sets relay state to 0

                    open = true;
                    close = false;
                    toggle = false;
                    reset = false;
                    setcounters = false;
                    break;  // next character

                case 't':
                    open = false;
                    close = false;
                    toggle = true;
                    reset = false;
                    setcounters = false;
                    break;  // next character

                case 'p':   // [P]ulse indicates that changes are pulsed

                    pulse = true;
                    break;  // next character

                case '=':   // acquire parameter
                    // extract just the content after the '='

                    StringBuffer sb = new StringBuffer();
                    for (n = i + 1; n < lowerCommand.length(); n++) {
                        char c = lowerCommand.charAt(n);
                        if (!Character.isDigit(c)) {
                            break;
                        }
                        sb.append(c);
                        i++;
                    }

                    String cmd = sb.toString(); //command.toString().substring(i+1).trim();
                    // check that only digits are here

                    for (n = 0; n < cmd.length(); n++) {
                        if (!Character.isDigit(cmd.charAt(n))) {
                            return null;
                            // obtain the integer value
                        }
                    }
                    parameter = new Integer(cmd).intValue();
                    value = true;
//                    stop = true;   // ends command interpretation
                    break;

                case 's':   // sets selected counters

                    dosetcounters = true;
                    setcounters = true;
                    open = false;
                    close = false;
                    toggle = false;
                    reset = false;
                    break;

                case '1':   // digit sets up state and mask

                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                    if (!open && !close && !toggle && !reset && !setcounters) {
                        return null;
                    }
                    n = (int) (lowerCommand.charAt(i) - '1');
                    if (shift == 0) {
                        mask |= (1 << n);
                        if (close) {
                            states |= (1 << n);
                        } else if (open) {
                            states &= ~(1 << n);
                        } else if (toggle) {
                            states ^= (1 << n);
//                            System.out.println("Toggle States: " + Integer.toBinaryString(states));
                        }
//                        else if (reset) res |= (1 << n);
//                        else if (setcounters) setmask |= (1 << n);
                    } else {
                        mask |= (1 << (n + 8 * shift));
                        if (close) {
                            states |= (1 << n << (8 * shift));
                        } else if (open) {
                            states &= ~(1 << n << (8 * shift));
                        } else if (toggle) {
                            states ^= (1 << n << (8 * shift));
//                            System.out.println("Toggle States: " + Integer.toBinaryString(states));
                        }
                    }
                    shift = 0;
                    break;  // next character

                case '+':
                    shift++;
                    break;

                case '*':    // means all - 12345678

                    if (!open && !close && !reset && !setcounters) {
                        return null;
                    }
                    mask |= 0xffff;
                    if (close) {
                        states = 0xffff;
                    } else if (open) {
                        states = 0;
                    }
//                    else if (reset) res = 0xff;
//                    else if (setcounters) setmask = 0xff;
                    break;  // next character

//                case 'l':   // list counters after command
//                    counterdump = true;
//                    break;
//
//                case 'u':   // list usage after command
//                    usagedump = true;
//                    break;

                case ' ':   // white space ignored

                    break;  // next character

                default:    // error in command
                //return false;

            }
        } /* each character in the command */

        // extra parameter is an error
        if (value && !pulse && !dosetcounters) {
            return null;
            // parameter conflict - can't use both [S]et and [P]ulse in the same command line.
        }
        if (value && pulse && dosetcounters) {
            return null;
            // [S]et Counters command requires a parameter
        }
        if (dosetcounters && !value) {
            return null;
            // if mask is nonzero then we have changes to make on the way out
        }
        if (mask != 0) {
//            if (DEBUG) System.out.println("Executing " + TINIOS.uptimeMillis() + " " + command.toString());

//            System.out.println(Long.toHexString(mask) + "  " + Long.toHexString(states));
            if (pulse) { /* this action is pulsed */

                if (dos != null) {
                    dos.writeByte(1);
                    dos.writeShort(mask);
                    dos.writeShort(states);
                    dos.writeInt(parameter);
                    JANOS.setOutputPulsed(states, mask, parameter);
                }
//                System.out.println("Pulse states=" + states + ", mask=" + mask + ", duration=" + parameter);
            } /* this action is pulsed */ else if (!toggle) {
                if (dos != null) {
                    dos.writeByte(2);
                    dos.writeShort(mask);
                    dos.writeShort(states);
                }
                JANOS.setOutputStates(states, mask);
//                System.out.println("Set states=" + states + ", mask=" + mask);
            } else if (toggle) {
                baos = new ByteArrayOutputStream();
                dos = new DataOutputStream(baos);
                JANOS.setOutputStates(states, mask);
            }
        }


//        long elapsed = TINIOS.uptimeMillis() - start;
//        System.out.println("DoCommand elapsed " + elapsed);
        if (baos != null) return baos.toByteArray();
        return null;
    }

}
