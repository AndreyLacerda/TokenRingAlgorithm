import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TokenRingClient extends JFrame implements ActionListener {
   private JTextField enter;
   private JTextArea display;

   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket socket;

   public TokenRingClient(){

      enter = new JTextField( "Mensagem: " );
      enter.addActionListener( this );
      getContentPane().add( enter, BorderLayout.NORTH );
      display = new JTextArea();
      getContentPane().add( new JScrollPane( display ),
                            BorderLayout.CENTER );
      setSize( 400, 300 );
      show();

      try {
         socket = new DatagramSocket();
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
            display.append("\n "+
               new String( receivePacket.getData(), 0,
                           receivePacket.getLength() ) );
               display.setCaretPosition(
                  display.getText().length() );
         }
         catch( IOException exception ) {
            display.append( exception.toString() + "\n" );
            exception.printStackTrace();
         }
      }
   }

   public void actionPerformed( ActionEvent e )
   {
      try {

         String s = e.getActionCommand();
         byte data[] = s.getBytes();

         sendPacket = new DatagramPacket( data, data.length,
            InetAddress.getLocalHost(), 5000 );
         socket.send( sendPacket );
         display.setCaretPosition(
            display.getText().length() );

      }
      catch ( IOException exception ) {
         display.append( exception.toString() + "\n" );
         exception.printStackTrace();
      }
   }

   public static void main( String args[] )
   {
	   TokenRingClient app = new TokenRingClient();

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
}