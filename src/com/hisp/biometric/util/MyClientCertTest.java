/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hisp.biometric.util;

import com.hisp.biometric.models.NetworkException;
import com.hisp.biometric.models.NetworkExceptionFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.ssl.SSLContexts;

/**
 *
 * @author Sidhanshu
 */
public class MyClientCertTest {
    
       private static final String KEYSTOREPATH = "KEYSTORE.pfx";
       private static final String KEYSTOREPASS = "keystorepass";
       private static final String KEYPASS = "keypass";
       
       SSLContext sslContext = null;
               
       private static MyClientCertTest myInstance = null;
       public static MyClientCertTest getInstane(){
           if(myInstance==null)return new MyClientCertTest();
           else return myInstance;
       }
       
       public synchronized SSLContext getSSLContext() throws NetworkException{
           
           try{
               
               if(sslContext == null ){
                   KeyStore keyStore = null;
                   //InputStream keyStoreStream = this.getClass().getResourceAsStream(KEYSTOREPATH);
                   FileInputStream instream = new FileInputStream(new File(KEYSTOREPATH));
                   keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(instream,KEYSTOREPASS.toCharArray());
                    sslContext = SSLContexts.custom()
                            .loadKeyMaterial(keyStore, KEYSTOREPASS.toCharArray())
                            .build();
                    return sslContext;
                    
               }else{
                   return sslContext;
               }
               
           }catch(Exception ex){
               throw NetworkExceptionFactory.getCustomException(ex+"");
           }
       }
       
       
               
    
}
