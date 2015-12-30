package com.prikshit.recorder.main;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 * <p/>
 * Class for storing the developers information
 * which is to be shown in About activity of our app.
 */
public class Information {
    String name;
    String email;
    int picId;

    /**
     * default constructor
     */
    public Information() {
        this.name = "";
        this.email = "";
        this.picId = 0;
    }

    /**
     * Constructor for with values passed
     *
     * @param name
     * @param email
     * @param picId
     */
    public Information(String name, String email, int picId) {
        this.name = name;
        this.email = email;
        this.picId = picId;
    }
}
