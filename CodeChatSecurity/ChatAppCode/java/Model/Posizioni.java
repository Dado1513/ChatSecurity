package com.example.dado.chatsecurity.Model;

import java.io.Serializable;

/**
 * Created by giuliotavella on 17/08/16.
 */
public class Posizioni  implements Serializable{

    public String posizioni_id;
    public String latitudine,longitudine;

    public Posizioni(String POSIZIONI_ID, String latitudine ,String longitudine) {
        this.posizioni_id = POSIZIONI_ID;
        this.latitudine = latitudine;
        this.longitudine = longitudine;


    }

    public String getPosizioniId()
    {
        return this.posizioni_id;
    }

    public String getLatitudine()
    {
        return this.latitudine;
    }

    public  String getLongitudine()
    {
        return this.longitudine;
    }

}
