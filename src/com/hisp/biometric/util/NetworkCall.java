/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hisp.biometric.util;

import com.hisp.biometric.login.LoginCredentials;
import com.hisp.biometric.main.FingerPrint;
import com.hisp.biometric.response.QueryResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.hisp.biometric.exception.*;
import com.zkteco.biometric.FingerPrintApplicationServerInstance;

/**
 *This class to be used only in client application 
 * @author Ahmed Ifhaam
 */
public class NetworkCall {
    public static LoginCredentials lc;
    public static final String DEFAULT_DOMAIN= "http://localhost:8080/fingerprint";
    public static final String API_SUFFIX = "/api/identify";
    public static String apiUrl;
    public static String HTTPSCHEME = "http";
    
    public static String dhis_domain;
    public static final String LATES_FID_SUFFIX = "/api/sqlViews/";//Ugohq30jgpi/data";
    public static String LATEST_FID_URL ;  //= "http://localhost:8080/dhis/api/sqlViews/Ugohq30jgpi/data";
    public static final String ALL_TEI_DATA_SUFFIX = "/api/trackedEntityInstances/query.json?ouMode=ALL";
    public static String ALL_TEI_DATA_URL; // = "http://localhost:8080/dhis/api/trackedEntityInstances/query.json?ouMode=ALL&attribute=ySaNYnlAMWL&attribute=ePbX8aM22Nb";
    
    private static ServerConfiguration config;
    
    //this method was used with client earlier
    public static void initString(LoginCredentials lcp){
        lc = lcp;
        String domain=lc.getUrl();
        if(domain==null){
            domain=DEFAULT_DOMAIN;
        }
        apiUrl=domain+API_SUFFIX;
    }
    
    public static void init(){
        config = ConfigurationAccess.getServerConfiguration();
        if(config!=null){
            dhis_domain = config.getDhisUrl();
            LATEST_FID_URL = dhis_domain+LATES_FID_SUFFIX+config.getSqlViewID()+"/data?paging=false";
            ALL_TEI_DATA_URL = dhis_domain+ALL_TEI_DATA_SUFFIX+"&attribute="
                    +config.getFingerprintStringAttribute()+"&attribute="+config.getFidAttribute();
            String tscheme = dhis_domain.substring(0,dhis_domain.indexOf(":"));
            if(tscheme.length()==4 || tscheme.length()==5) HTTPSCHEME = tscheme;
            
        }else{
            //System.out.println("Configuration File not found Default would be loaded ");
            Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"Configuration File not found Default would be loaded ");
            config = ServerConfiguration.getDefault();
            ConfigurationAccess.saveServerConfiguration(config);
            dhis_domain = config.getDhisUrl();
            LATEST_FID_URL = dhis_domain+LATES_FID_SUFFIX+config.getSqlViewID()+"/data?paging=false";
            ALL_TEI_DATA_URL = dhis_domain+ALL_TEI_DATA_SUFFIX+"&attribute="
                    +config.getFingerprintStringAttribute()+"&attribute="+config.getFidAttribute();
            
        }
        System.out.println("Dhis Domain : "+dhis_domain);
        Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"SCHEME : "+HTTPSCHEME);
        Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"Dhis Domain : "+dhis_domain);
        Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"LATEST FID URL : "+LATEST_FID_URL);
        Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"ALL TEI DATA URL : "+ALL_TEI_DATA_URL);
    }
    
    public static FingerPrint sendEnrollment(String template) throws NetworkException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        FingerPrint fp = new FingerPrint();
        fp.setTemplate(template);
        fp.setFid(-1);
        try{

            HttpPut request = new HttpPut(apiUrl);
            
            String json = fp.toString();
            StringEntity entity = new StringEntity(json);
            request.setEntity(entity);
            request.setHeader("Content-type","application/json");
            
            
            
            //Future<HttpResponse> future = httpclient.execute(request,null);
            //HttpResponse response = future.get();
            CloseableHttpResponse response = httpclient.execute(request);
            if(response.getStatusLine().getStatusCode()==200){
                //System.out.println(response.getStatusLine());
                InputStream is = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String responseStr = "";
                while(reader.ready()){
                    responseStr +=reader.readLine();
                }
                fp = FingerPrint.fromJson(responseStr);
                //System.out.println(responseStr);
            }else{
                System.out.println("Request : "+request.toString());
                Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.WARNING,"Request : "+request.toString());
                NetworkException exception = NetworkExceptionFactory.getException(response.getStatusLine().getStatusCode());
                System.out.print(exception);
                throw exception;
            }
            
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(NetworkCall.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(NetworkCall.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return fp;
    }
    
    public static FingerPrint recognize(String template) throws NetworkException{
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        FingerPrint fp = new FingerPrint();
        fp.setTemplate(template);
        
        try{
            
            HttpPost request = new HttpPost(apiUrl);
            
            String json = fp.toString();
            
            StringEntity entity = new StringEntity(json);
            request.setEntity(entity);
            request.setHeader("Content-type","application/json");
            
            
            
            //Future<HttpResponse> future = httpclient.execute(request,null);
            //HttpResponse response = future.get();
            CloseableHttpResponse response = httpclient.execute(request);
            if(response.getStatusLine().getStatusCode()==200){
                //System.out.println(response.getStatusLine());
                InputStream is = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String responseStr = "";
                while(reader.ready()){
                    responseStr +=reader.readLine();
                }
                fp = FingerPrint.fromJson(responseStr);
                //System.out.println(responseStr);
            }else{
                System.out.println("Request : "+request.toString());
                Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.WARNING,"Request : "+request.toString());
                NetworkException exception = NetworkExceptionFactory.getException(response.getStatusLine().getStatusCode());
                System.out.print(exception);
                response.close();
                httpclient.close();
                throw exception;
            }
            
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(NetworkCall.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }catch (IOException ex) {
            Logger.getLogger(NetworkCall.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return fp;
    }
    
    public static int getLatestFid() throws NetworkException{
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        HttpHost targetHost = new HttpHost(config.getHost(), config.getPort(), HTTPSCHEME);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials("hispdev", "Devhisp@1"));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        //System.out.println(LATEST_FID_URL);
        HttpGet httpget = new HttpGet(LATEST_FID_URL);
        System.out.println("LT FID URL :"+LATEST_FID_URL);
        //for (int i = 0; i < 3; i++) {
            QueryResponse responseMapped;
        try {
            CloseableHttpResponse response = httpclient.execute(
                targetHost, httpget, context);
            if(response.getStatusLine().getStatusCode()==200){
                HttpEntity entity = response.getEntity();
                String st = EntityUtils.toString(entity);
                responseMapped = QueryResponse.fromJson(st);

                System.out.println("-------------------------------");
                //System.out.println(st);
                System.out.println("Latest FID "+responseMapped.getFID());
                Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"Latest FID "+responseMapped.getFID());
                response.close();
                httpclient.close();
            }else{
                
                NetworkException exception = NetworkExceptionFactory.getException(response.getStatusLine().getStatusCode());
                System.out.print(exception);
                response.close();
                httpclient.close();
                throw exception;
            }
            
            
        } catch(IOException ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return -1;
        }
        return responseMapped.getFID();
        
    }
    
    
    
    public static List<FingerPrint> getAllFingerPrints() throws NetworkException{
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        HttpHost targetHost = new HttpHost(config.getHost(), config.getPort(), HTTPSCHEME);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials("hispdev", "Devhisp@1"));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        QueryResponse responseMapped;
        HttpGet httpget = new HttpGet(ALL_TEI_DATA_URL);
        //for (int i = 0; i < 3; i++) {
            
        try {
            CloseableHttpResponse response = httpclient.execute(
                targetHost, httpget, context);
            if(response.getStatusLine().getStatusCode()==200){
                HttpEntity entity = response.getEntity();
                String st = EntityUtils.toString(entity);
                responseMapped = QueryResponse.fromJson(st);
                System.out.println("-------------------------------");
            }else{
                response.close();
                httpclient.close();
                NetworkException exception = NetworkExceptionFactory.getException(response.getStatusLine().getStatusCode());
                System.out.print(exception);
                response.close();
                httpclient.close();
                throw exception;
            }
            
            //System.out.println(st);
            
            response.close();
            httpclient.close();
            return responseMapped.getFingerPrints();
            //return responseMapped.getFID();
        } catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
    }
    
    
}
