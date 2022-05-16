package com.example.artbookjava;

import java.io.Serializable;

public class Art implements Serializable {
    String name ;
    int id;

    public Art(String name , int id){   // constructor tanımladım
        this.name = name ;
        this.id = id;


    }

}
