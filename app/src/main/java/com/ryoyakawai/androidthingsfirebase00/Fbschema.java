package com.ryoyakawai.androidthingsfirebase00;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by kawai on 5/22/17.
 */

@IgnoreExtraProperties
public class Fbschema {

    private int interval;

    public Fbschema(){
    }

    public int getInterval() {
        return interval;
    }
}
