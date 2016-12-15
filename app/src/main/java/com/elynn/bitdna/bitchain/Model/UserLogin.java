package com.elynn.bitdna.bitchain.Model;

import java.util.Date;

/**
 * Created by Aryo on 27/10/2016.
 */

public class UserLogin {
    public static int userId;
    public static String email;
    public static String password;
    public static String firstName;
    public static String lastName;
    public static String google2FAsecret;
    public static String QrCodeUrl;
    public static String token;
    public static String dob;
    public static String mobilePhone;
    public static boolean enable;
    public static boolean validSecret;
    public static boolean isFingerPrintEnabled;
    public static String deviceId;
    public static boolean isRemoteKick;

    public static String getName() {
        return firstName + " "+lastName;
    }
    public static String getId(){return String.valueOf(userId);}
}
