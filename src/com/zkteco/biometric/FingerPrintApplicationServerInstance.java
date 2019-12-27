/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zkteco.biometric;

import com.hisp.biometric.exception.NetworkException;
import com.hisp.biometric.main.FingerPrint;
import com.hisp.biometric.util.NetworkCall;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ahmed Ifhaam
 */
public class FingerPrintApplicationServerInstance {
    
    private static final long serialVersionUID = 1L;
    private static final int TEMPLATE_SIZE = 2048;
    
    private static long mhDB = 0;
    private static int lastFID = -1;
    
    
    
    public static FingerPrintApplicationServerInstance instance = null;
    private FingerPrintApplicationServerInstance(){
        NetworkCall.init();
        initializeDB();
        System.out.println("Total Fingerprint counts : "+countDBIns());
        Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"Total Fingerprint counts : "+countDBIns());
    }
    
    /* removing things to make the class thread safe
    public static FingerPrintApplicationServerInstance getInstance(){
        if(instance == null){
            instance = new FingerPrintApplicationServerInstance(); 
        }
        
        return instance;
    }*/
    
    public static FingerPrintApplicationServerInstance getInstance(){
        if(instance==null){
            synchronized(FingerPrintApplicationServerInstance.class){
                if(null==instance){
                    instance =new FingerPrintApplicationServerInstance();
                }
            }
        }
        return instance;
    }
    
    public synchronized boolean initializeDB(){
        if (FingerprintSensorErrorCode.ZKFP_ERR_OK != FingerprintSensorEx.Init())
        {
            System.out.println("Fingerprint Sensor Initialization failed!");
            Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.SEVERE,"Fingerprint Sensor Initialization failed");
            
        }
        if (0 == (mhDB = FingerprintSensorEx.DBInit())){
            System.out.println("Fingerprint Database Initialization failed");
            Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.SEVERE,"Fingerprint Database Initialization failed");
            return false;
        }else{
            int fid;
            //fid = 4789;
            
            try{
                fid = NetworkCall.getLatestFid();  
            }catch(NetworkException ex){
                ex.printStackTrace();
                fid = -1;
                
            }
            
            if(fid==-1){
                System.out.println("Error retriving fid");
                Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.SEVERE,"Error Retriving FID");
                return false;
            }else{
                lastFID = fid;
                System.out.println("Latest Fid :"+ lastFID);
                Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"Latest FID -- "+ lastFID);
                List<FingerPrint> fingerprints;
                
                try {
                    fingerprints = NetworkCall.getAllFingerPrints();
                    System.out.println("fingerprints size :"+ fingerprints.size());
                } catch (NetworkException ex) {
                    Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                    fingerprints = null;
                }
                if(fingerprints!=null && addDataSet(fingerprints)){
                    
                    System.out.println("DB init success");
                    return true;
                }else{
                    System.out.println("Failed to sync data with dhis");
                    return false;
                }
                
                
            }
            
        }
        
    }
    
    
    public boolean addDataSet(List<FingerPrint> fps){
        int success=0;
        for(FingerPrint fp :fps){
            byte[] template = new byte[TEMPLATE_SIZE];
            int ret = FingerprintSensorEx.Base64ToBlob(fp.getTemplate(),template,TEMPLATE_SIZE);
            if(ret!=-1 && addToDb(template, fp.getFid())){
                success++;
            }else{
                System.out.println("Failed for :"+fp.getFid());
                Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.SEVERE, "Failedfor "+fp.getFid());
            }
            
        }
        if(fps.size()==success){
            return true;
        }else{
            return false;
        }
    }
    
    public FingerPrint deleteFingerprint(FingerPrint fp){
        if(fp.getFid()>0){
           int result =  FingerprintSensorEx.DBDel(mhDB, fp.getFid());
           if(result==0){
               
               return fp;
           }else{
               fp.setFid(-1);
               return fp;
           }
        }
        fp.setFid(-1);
        return fp;
    }
    
    public boolean freeDB(){
        FingerprintSensorEx.DBFree(mhDB);
        mhDB = 0;
        return true;
        
    }
    
    public boolean addToDb(byte[] template,int fid){
        int ret = -1;
        if(0 == (ret = FingerprintSensorEx.DBAdd(mhDB, fid, template))){
            return true;
        }
        return false;
    }
    
    public int countDBIns(){
        return FingerprintSensorEx.DBCount(mhDB);
    }
    
    public int identify(byte[] template){
        
        int[] fid = new int[1];
        int[] score = new int [1];
        int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
        if(ret==0)return fid[0];
        else return -1;
    }
    
    
    //methods to call from server
    /**
     * 
     * @param modelJSON takes a model object in json string format and try to enroll it in to the system  
     * @return if succeeds fid for the fingerprint 
     *          else -1
     */
    public synchronized String enroll(String modelJSON){
        FingerPrintApplicationServerInstance instance = FingerPrintApplicationServerInstance.getInstance();
        //if(instance.initializeDB())System.out.println("Success");
        byte[] template = new byte[2048];
        FingerPrint fp = FingerPrint.fromJson(modelJSON);
        FingerprintSensorEx.Base64ToBlob(fp.getTemplate(), template, TEMPLATE_SIZE);
        int fid = lastFID+1;
        System.out.println(""+fid);
        System.out.println(fp.getTemplate());
        boolean ret = instance.addToDb(template, fid);
        //System.out.println(modelJSON);
        if(ret){
            
            System.out.println("Object enrolled");
            Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.INFO,"Object enrolled +"+fid);
            fp.setFid(fid);
            lastFID=fid;
            return fp.toString();
        }
        else{
            System.out.println("Object failed");
            Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.WARNING,"Object Failed FID");
            fp.setFid(-1);
            return fp.toString();
        }
    }
    
    /**
     * 
     * @param modelJson takes a model object in json string format and try recognize it in to the system 
     * @return if recognized returns the fid or else returns -1
     */
    
    public synchronized String recognize(String modelJson){
        
        FingerPrint fingerPrint = FingerPrint.fromJson(modelJson);
        String tempStr = fingerPrint.getTemplate();
        byte[] template = new byte[TEMPLATE_SIZE];
        FingerprintSensorEx.Base64ToBlob(tempStr, template, TEMPLATE_SIZE);
        FingerPrintApplicationServerInstance instance = FingerPrintApplicationServerInstance.getInstance();
        int fid =instance.identify(template);
        fingerPrint.setFid(fid);
        return fingerPrint.toString();
        
    }
    
    
    /**
     * 
     * @param fid to delete 
     * @return fingerprint with the same fid if deletion succeeds else returns -1 as the fid
     */
    public synchronized String delete(String fid){
        FingerPrint fingerPrint = new FingerPrint();
        try{
            System.out.println(fid);
        fingerPrint.setFid(Integer.parseInt(fid));
        FingerPrintApplicationServerInstance instance = FingerPrintApplicationServerInstance.getInstance();
        fingerPrint = instance.deleteFingerprint(fingerPrint);
        }catch(Exception ex){
            ex.printStackTrace();
            Logger.getLogger(FingerPrintApplicationServerInstance.class.getName()).log(Level.SEVERE, null,ex);
            
            fingerPrint.setFid(-1);
            
        }
        return fingerPrint.toString();
    }
    
}

