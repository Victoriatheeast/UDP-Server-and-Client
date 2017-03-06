//Author: Victoria Wu

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Client can generate multiple ping requests and recieve responses from server over UDP concurrently.
 *
 */

public class PingClient implements Runnable {

    //Space
    private static final String space=" ";

    //Round trip times
    private static ArrayList<Integer> rtts = new ArrayList<>();

    //Server IP Address in string format
    private static String serverIP = "";

    //Server port
    private static int serverPort = -1;

    //The number of ping messages to send
    private static int count = 0;

    //Timeout value
    private static int timeout = 0;

    //Period value
    private static int period = 0;

    //Number of packets received
    private static int countReceived = 0;

    //Starting time
    private static long start = 0;

    //Ending time
    private static long end = 0;

    //Properties
    private int sequence_number;



    /**
     * Constructs a ping client
     */

    public PingClient(int sequence_number){
        this.sequence_number = sequence_number;
    }

    /**
     * Implement the run() method of the Runnable interface
     */
    public void run(){

        try {
            //Get IP address from the command line input
            InetAddress server = InetAddress.getByName(serverIP);

            // Create a datagram socket for sending and receiving UDP packets
            DatagramSocket socket = new DatagramSocket();

            if(this.sequence_number == 0) {
                System.out.println("PING" + space + serverIP);
            }

            byte[] buf = new byte[1024];

            // Timestamp in ms when we send it and add to list
            long tsSend = System.currentTimeMillis();
            if(this.sequence_number == 0){
                start = tsSend;
            }

            // Create string to send, and transform it into a Byte Array
            String str = "PING" + space + this.sequence_number + space + tsSend + " \r\n";
            buf = str.getBytes();

            // Create a datagram packet to send as an UDP packet.
            DatagramPacket ping = new DatagramPacket(buf, buf.length, server, serverPort);

            // Send the Ping datagram to the specified server

            socket.send(ping);


            try{  // Set up the timeout
                socket.setSoTimeout(timeout);
                // Build up an UPD packet for receiving
                DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                // Try to receive the response
                socket.receive(response);
                // If the response is received
                // Record timestamp for the received packet
                long tsReceived = System.currentTimeMillis();
                end = tsReceived;

                countReceived++;

                // Print individual statistics as replies are received
                printData(response, tsReceived - tsSend);


                // If the packet is not received, it will continue in the catch
            } catch (IOException e) {

                // Record the timestamp for the timeout end for the current expected packet
                long msTimeout = System.currentTimeMillis();
                end = msTimeout;
                
                //Print the timestamp for timeout
                //System.out.println("Timeout end for packet" + this.sequence_number);

            }

            } catch (SocketException e){
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }

    }


    public static void main(String[] args){

        // Process command-line arguments. to do
        for(String arg : args){
            String[] splitArg = arg.split("=");
            if(splitArg.length == 2 && splitArg[0].equals("--server_ip")){
                serverIP = splitArg[1];
            } else if (splitArg.length == 2 && splitArg[0].equals("--server_port")){
                serverPort = Integer.parseInt(splitArg[1]);
            } else if (splitArg.length == 2 && splitArg[0].equals("--count")){
                count = Integer.parseInt(splitArg[1]);
            } else if (splitArg.length == 2 && splitArg[0].equals("--period")){
                period = Integer.parseInt(splitArg[1]);
            } else if (splitArg.length == 2 && splitArg[0].equals("--timeout")){
                timeout = Integer.parseInt(splitArg[1]);
            } else {
                System.err.println("Usage: java PingClient --server_ip=<server ip addr> "+
                        "--server_port=<server port> " +
                        "--count=<number of pings to send> " +
                        "--period=<wait interval> " +
                        "--timeout=<timeout>");
                return;
            }

        }

        //Check IP address
        if (serverIP.isEmpty()){
            System.err.println("Must specify server IP address with --serverIP");
            return;
        }

        // Check port number.
        if (serverPort == -1) {
            System.err.println("Must specify server port with --serverPort");
            return;
        }
        if (serverPort <= 1024) {
            System.err.println("Avoid potentially reserved port number: " + serverPort + " (should be > 1024)");
            return;
        }
        // Check count number
        if (count < 0){
            System.err.println("Must specify number of pings to send with --count");
            return;
        }

        //Starting time
        Thread[] threads = new Thread[count];

        try{
            for(int i = 0; i < count; i++) {
                PingClient pc = new PingClient(i);
                threads[i] = new Thread(pc);
                threads[i].start();

                Thread.currentThread().sleep(period);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //If the number of current running threads is more than one, then does not continue to the summary.
        while (Thread.activeCount() != 1);


        printSummary(serverIP, count, start, end);

    }

    /**
     * Print ping data to the standard output stream.
     * @param request the received datagram packet
     */
    private static void printData(DatagramPacket request, long delayTime) throws IOException
    {

        // Obtain references to the packet's array of bytes.
        byte[] buf = request.getData();

        // Wrap the bytes in a byte array input stream,
        // so that you can read the data as a stream of bytes.
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);

        // Wrap the byte array output stream in an input stream reader,
        // so you can read the data as a stream of characters.
        InputStreamReader isr = new InputStreamReader(bais);

        // Wrap the input stream reader in a buffered reader,
        // so you can read the character data a line at a time.
        // (A line is a sequence of chars terminated by any combination of \r and \n.)
        BufferedReader br = new BufferedReader(isr);

        // The message data is contained in a single line, so read this line.
        String line = br.readLine();

        //Get the sequence number in the response
        String[] decompose = line.split(space);

        //Calculate the round trip time for the packet with the same sq in the request
        rtts.add((int)delayTime);

        // Print host address and data received from it.
        System.out.println(
                "PONG " +
                        request.getAddress().getHostAddress() +
                        ": " + "seq=" + decompose[1] + space
                        + "time=" + delayTime + "ms");

    }


    /**
     * Print ping summary statistics to the standard output stream
     * @param serverIP
     * @count number of packets
     * @start starting timestamp
     * @end ending timestamp
     */
    private static void printSummary(String serverIP, int count, long start, long end){
        System.out.println("\n--- "+serverIP+space+"ping statistics ---");

        StringBuilder sb = new StringBuilder()
                    .append(count + space + "transmitted, ")
                    .append(countReceived + space + "received, ")
                    .append(String.format("%.02f",(1 - (double) countReceived / count) * 100) + "% loss, ")
                    .append("time " + (int) (end - start) + space + "ms");

        System.out.println(sb.toString());

        printStatistics(rtts);

    }

    /**
     * Printout the min/avg/max data to the standard output stream
     * @param rtts round trip times array list
     */

    private static void printStatistics(ArrayList<Integer> rtts){

        if(rtts.isEmpty()){
            System.out.println("rtt min/avg/max = "+"0/0/0");
        } else {
            Collections.sort(rtts);
            int sum = 0;
            for (int rtt : rtts) {
                sum += rtt;
            }
            int avg = sum / rtts.size();
            System.out.println("rtt min/avg/max = " + rtts.get(0) + "/" + avg + "/" + rtts.get(rtts.size() - 1));
        }

    }
}
