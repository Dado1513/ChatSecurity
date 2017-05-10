package com.example.dado.chatsecurity.Model;


import java.io.Serializable;




public class Message implements Serializable {

    private String message_id, message_id_conversazione , message_text,  ora_message,id_user;
    boolean is_image;

    public Message(String MESSAGGI_ID, String MESSAGGI_ID_CONVERSAZIONE ,String id_user,String MESSAGGI_TESTO, String ORA_MESSAGGIO, boolean MESSAGGI_IS_IMAGE) {
        this.message_id = MESSAGGI_ID;
        this.message_id_conversazione = MESSAGGI_ID_CONVERSAZIONE;
        this.message_text = MESSAGGI_TESTO;
        this.ora_message = ORA_MESSAGGIO;
        this.is_image = MESSAGGI_IS_IMAGE;
        this.id_user=id_user;
    }

    public String getMessageId()
    {
        return this.message_id;
    }

    public String getIdConversaione()
    {
        return this.message_id_conversazione;
    }

    public  String getTesto()
    {
        return this.message_text;
    }

    public void setMessage_text(String text){
        this.message_text=text;
    }
    public boolean getIsImage(){return is_image;}

    public String getIdUser(){
        return id_user;
    }
    public  String getOraMessaggio()
    {
        return this.ora_message;
    }


}
