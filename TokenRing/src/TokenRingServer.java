import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TokenRingServer extends JFrame {
   private JTextArea display;

   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket socket;
   
   private LinkedHashMap <InetAddress, Integer> nodesIp = new LinkedHashMap<InetAddress, Integer>();
   private int tokenPosition;
  
   public TokenRingServer(){
	  tokenPosition = 0;
      display = new JTextArea();
      getContentPane().add( new JScrollPane( display),
                            BorderLayout.CENTER );
      setSize( 400, 300 );
      show();

      try {
         socket = new DatagramSocket( 5000 );
      }
      catch( SocketException se ) {
         se.printStackTrace();
         System.exit( 1 );
      }
   }

   public void waitForPackets()
   {
      while ( true ) {
         try {
            // set up packet
            byte data[] = new byte[ 100 ];
            receivePacket =
               new DatagramPacket( data, data.length );

            // wait for packet
            socket.receive( receivePacket );
 
            // process packet
            display.append( "\nPacket received:" +
               "\nFrom host: " + receivePacket.getAddress() +
               "\nHost port: " + receivePacket.getPort() +
               "\nLength: " + receivePacket.getLength() +
               "\nContaining:\n\t" +
               new String( receivePacket.getData(), 0,
                           receivePacket.getLength() ) );

            // echo information from packet back to client
            display.append( "\n\nEcho data to client...");
            if (!nodesIp.containsKey(receivePacket.getAddress())) {
            	nodesIp.put(receivePacket.getAddress(), receivePacket.getPort());
            	String texto = "Você é o "+ nodesIp.size() + "º nó";
            	byte[] mensagem = texto.getBytes();
            	sendPacket =
                        new DatagramPacket( mensagem,
                        					mensagem.length,
                                            receivePacket.getAddress(),
                                            receivePacket.getPort()
                                             );
                    socket.send( sendPacket );
                if(tokenPosition == 0) {
                	tokenPosition ++;
                	texto = "Você ESTÁ com o TOKEN agora";
                	mensagem = texto.getBytes();
                	sendPacket =
                            new DatagramPacket( mensagem,
                            					mensagem.length,
                                                receivePacket.getAddress(),
                                                receivePacket.getPort()
                                                 );
                        socket.send( sendPacket );
                }
            }
            else
            	if (!nodesIp.get(receivePacket.getAddress()).equals(receivePacket.getPort()))
            		nodesIp.replace(receivePacket.getAddress(), receivePacket.getPort());
            
            ArrayList<InetAddress> ips = new ArrayList<InetAddress>(nodesIp.keySet());
       
        	if (!ips.get(tokenPosition-1).equals(receivePacket.getAddress())) {
        		System.err.println(ips.get(tokenPosition-1));
        		System.err.println(receivePacket.getAddress());
        		String texto = "Sua mensagem NÃO foi enviado, pois você NÃO possui o TOKEN no momento";
            	byte[] mensagem = texto.getBytes();
        		sendPacket =
                        new DatagramPacket( mensagem,
                        					mensagem.length,
                                            receivePacket.getAddress(),
                                            receivePacket.getPort()
                                             );
                    socket.send( sendPacket );
                    display.append( "Pacote Enviado\n" );
        	} else {
        		for (Map.Entry<InetAddress, Integer> node : nodesIp.entrySet()) {
                	InetAddress ip = node.getKey();
                	Integer porta = node.getValue();
            		String texto = new String(receivePacket.getData(), 0,
                            receivePacket.getLength() );
            		texto = tokenPosition+"º Nó: " + texto;
            		byte[] mensagem = texto.getBytes();
            		sendPacket =
                            new DatagramPacket( mensagem,
                                                texto.length(),
                                                ip,
                                                porta
                                                 );
                        socket.send( sendPacket );
                        display.append( "Packet sent\n" );
                }
        		if (tokenPosition + 1 > nodesIp.size())
        			tokenPosition = 1;
        		else
        			tokenPosition ++;
        		mensagemToken(ips);
        	}   
            display.setCaretPosition(
            display.getText().length() );
         }
         catch( IOException io ) {
            display.append( io.toString() + "\n" );
            io.printStackTrace();
         }
      }
   }

   public static void main( String args[] )
   {
	   TokenRingServer app = new TokenRingServer();

      app.addWindowListener(
         new WindowAdapter() {
            public void windowClosing( WindowEvent e )
            {
               System.exit( 0 );
            }
         }
      );

      app.waitForPackets();
   }
   
   private void mensagemToken(ArrayList<InetAddress> ips) throws IOException {
	   System.err.println(tokenPosition);
	   InetAddress ip = ips.get(tokenPosition - 1);
	   Integer porta = nodesIp.get(ip);
	   
	   String texto = "Você ESTÁ com o TOKEN agora";
	   byte[] mensagem = texto.getBytes();
	   sendPacket =
               new DatagramPacket( mensagem,
                                   mensagem.length,
                                   ip,
                                   porta
                                    );
      socket.send( sendPacket );
   }
}