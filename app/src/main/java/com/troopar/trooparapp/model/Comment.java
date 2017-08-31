package com.troopar.trooparapp.model;

import java.io.Serializable;

/**
 * Created by Barry on 19/01/2016.
 */
public class Comment implements Serializable{

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    private String name;
    private String date;

    public Comment(String content, String date, String name) {
        this.content = content;
        this.date = date;
        this.name = name;
    }

    private String content;


}
