package com.elynn.bitdna.bitchain.Model;

/**
 * Created by Aryo on 28/10/2016.
 */

public class PathConfiguration {
    public static String CHECKVALIDATIONGOOGLEAUTHSECRET = AppConfiguration.URLPATH + "/api/auth/checkValidationGoogleAuthSecret";
    public static String LOGIN = AppConfiguration.URLPATH + "/api/auth/login";
    public static String LOGINVIAFINGERPRINT = AppConfiguration.URLPATH + "/api/auth/loginfingerprint";
    public static String GETFILES = AppConfiguration.URLPATH + "/api/files/getFiles";
    public static String GENERATESECRETKEYS = AppConfiguration.URLPATH + "/api/auth/generateSecretKey";
    public static String ENABLEFINGERPRINT = AppConfiguration.URLPATH + "/api/auth/enableFingerPrint";
    public static String CHECKREGISTEREDDEVICEID = AppConfiguration.URLPATH + "/api/auth/checkRegisteredDeviceId";
    public static String DELETEFILE = AppConfiguration.URLPATH + "/api/files/deleteFile";
    public static String SHAREFILE = AppConfiguration.URLPATH + "/api/share/";
    public static String LISTSHARED = AppConfiguration.URLPATH + "/api/files/listShared/";
    public static String REVOKEFILE = AppConfiguration.URLPATH + "/api/share/revokeFile";
    public static String UPLOADFILE = AppConfiguration.URLPATH + "/api/files/uploadFile";
    public static String GETFILE = AppConfiguration.URLPATH + "/api/files/getfile/";
    public static String REGISTER = AppConfiguration.URLPATH + "/api/auth/register";
    public static String CHANGEPASSWORD = AppConfiguration.URLPATH + "/api/password/changepassword";
    public static String RESETPASSWORD = AppConfiguration.URLPATH + "/api/password/reset";

}
