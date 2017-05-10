package com.example.dado.chatsecurity.Model;


import java.io.Serializable;

public class Conversazioni implements Serializable,Comparable<Conversazioni>{

    private String conversazioni_id,conversazioni_id_utente;
    int unreadCount;
    String lastMessage,timestamp;

    public Conversazioni(){

    }

    public Conversazioni(String CONVERSAZIONI_ID, String CONVERSAZIONI_ID_UTENTE) {
        this. conversazioni_id = CONVERSAZIONI_ID;
        this.conversazioni_id_utente = CONVERSAZIONI_ID_UTENTE;

    }

    public Conversazioni(String CONVERSAZIONI_ID, String CONVERSAZIONI_ID_UTENTE,String unreadCount) {
        this. conversazioni_id = CONVERSAZIONI_ID;
        this.conversazioni_id_utente = CONVERSAZIONI_ID_UTENTE;
        this.unreadCount=Integer.valueOf(unreadCount);

    }

    public Conversazioni(String CONVERSAZIONI_ID, String CONVERSAZIONI_ID_UTENTE,String lastMessage,String timestamp) {
        this. conversazioni_id = CONVERSAZIONI_ID;
        this.conversazioni_id_utente = CONVERSAZIONI_ID_UTENTE;
        this.lastMessage=lastMessage;
        this.timestamp=timestamp;
        this.unreadCount=0;

    }

    public Conversazioni(String CONVERSAZIONI_ID, String CONVERSAZIONI_ID_UTENTE,String lastMessage,String timestamp,int unreadCount) {
        this. conversazioni_id = CONVERSAZIONI_ID;
        this.conversazioni_id_utente = CONVERSAZIONI_ID_UTENTE;
        this.lastMessage=lastMessage;
        this.timestamp=timestamp;
        this.unreadCount=unreadCount;

    }
    public String getConversazioniId() {
        return this.conversazioni_id;
    }

    public String getIdUtente() {
        return this.conversazioni_id_utente;
    }

    public String getLastMessage(){
        return this.lastMessage;
    }

    public int getUnreadCount(){
        return unreadCount;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public void azzeraUnreadCount(){
        this.unreadCount=0;
    }

    @Override
    public int compareTo(Conversazioni another) {
        if (another != null && this.getTimestamp()!=null){
            if (this.getTimestamp().compareTo(another.getTimestamp()) > 0)
                return -1;
            else
                return 1;
        }else{
            return 0;
        }
    }

    public void setID(String id){
        this.conversazioni_id=id;

    }
    public void addUnreadCount(){
        this.unreadCount+=1;
    }

    public void setLastMessage(String message){
        this.lastMessage=message;
    }
    public void setTimestamp(String timestamp){
        this.timestamp=timestamp;
    }

    public void setUnreadCount(int unreadCount){
        this.unreadCount=unreadCount;
    }
}
