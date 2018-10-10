/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hisp.biometric.util;

/**
 *
 * @author Sidhanshu
 */
import java.io.BufferedReader;
  import java.io.InputStreamReader;
  import java.io.OutputStreamWriter;
  import java.io.Writer;
  import java.net.Socket;

  import javax.net.ssl.SSLSocketFactory;

  public class Test {
        
     public static final String TARGET_HTTPS_SERVER = "tracker.hivaids.gov.np"; 
     public static final int    TARGET_HTTPS_PORT   = 443; 
        
     public static void main(String[] args) throws Exception {
        
//       Socket socket = SSLSocketFactory.getDefault().
//         createSocket(TARGET_HTTPS_SERVER, TARGET_HTTPS_PORT);
//       try {
//         Writer out = new OutputStreamWriter(
//            socket.getOutputStream(), "ISO-8859-1");
//         out.write("GET / HTTP/1.1\r\n");  
//         out.write("Host: " + TARGET_HTTPS_SERVER + ":" + 
//             TARGET_HTTPS_PORT + "\r\n");  
//         out.write("Agent: SSL-TEST\r\n");  
//         out.write("\r\n");  
//         out.flush();  
//         BufferedReader in = new BufferedReader(
//            new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
//         String line = null;
//         while ((line = in.readLine()) != null) {
//            System.out.println(line);
//         }
//       } finally {
//         socket.close(); 
//       }

            System.out.println(System.getProperty("javax.net.ssl.trustStore")+"");
     }
  }
       