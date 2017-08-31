package com.troopar.trooparapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.google.android.gms.maps.model.BitmapDescriptor;

/**
 * Created by Barry on 15/07/2016.
 * global application context
 */
public class ApplicationContextStore {

    private static ApplicationContextStore ourInstance = new ApplicationContextStore();
    private BitmapDrawable BACKICON;
    private BitmapDescriptor MAPLOCATIONICON;
    private Bitmap MAPLOCATIONICONBITMAP;
    private Bitmap CHATPEOPLEINFOICON;
    private Bitmap MESSAGECHOOSEPHOTOICON;
    private Bitmap MESSAGECAMERAICON;
    private Context context;

    public static ApplicationContextStore getInstance() {
        return ourInstance;
    }


    private ApplicationContextStore() {
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BitmapDrawable getBACKICON() {
        return BACKICON;
    }

    public void setBACKICON(BitmapDrawable BACKICON) {
        this.BACKICON = BACKICON;
    }

    public Bitmap getCHATPEOPLEINFOICON() {
        return CHATPEOPLEINFOICON;
    }

    public void setCHATPEOPLEINFOICON(Bitmap CHATPEOPLEINFOICON) {
        this.CHATPEOPLEINFOICON = CHATPEOPLEINFOICON;
    }

    public BitmapDescriptor getMAPLOCATIONICON() {
        return MAPLOCATIONICON;
    }

    public void setMAPLOCATIONICON(BitmapDescriptor MAPLOCATIONICON) {
        this.MAPLOCATIONICON = MAPLOCATIONICON;
    }

    public Bitmap getMAPLOCATIONICONBITMAP() {
        return MAPLOCATIONICONBITMAP;
    }

    public void setMAPLOCATIONICONBITMAP(Bitmap MAPLOCATIONICONBITMAP) {
        this.MAPLOCATIONICONBITMAP = MAPLOCATIONICONBITMAP;
    }

    public Bitmap getMESSAGECAMERAICON() {
        return MESSAGECAMERAICON;
    }

    public void setMESSAGECAMERAICON(Bitmap MESSAGECAMERAICON) {
        this.MESSAGECAMERAICON = MESSAGECAMERAICON;
    }

    public Bitmap getMESSAGECHOOSEPHOTOICON() {
        return MESSAGECHOOSEPHOTOICON;
    }

    public void setMESSAGECHOOSEPHOTOICON(Bitmap MESSAGECHOOSEPHOTOICON) {
        this.MESSAGECHOOSEPHOTOICON = MESSAGECHOOSEPHOTOICON;
    }


}
