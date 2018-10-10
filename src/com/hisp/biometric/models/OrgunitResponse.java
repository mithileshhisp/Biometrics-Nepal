/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hisp.biometric.models;

import com.google.gson.Gson;

/**
 *
 * @author Sidhanshu
 */
public class OrgunitResponse {
    String id;
    String displayName;

    public static  OrgunitResponse fromJson(String jsonStr){
        return new Gson().fromJson(jsonStr, OrgunitResponse.class);
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    
}
