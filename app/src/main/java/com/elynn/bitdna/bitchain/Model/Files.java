package com.elynn.bitdna.bitchain.Model;

/**
 * Created by Aryo on 04/11/2016.
 */

public class Files {
    public int id;
    public int userId;
    public int fileParent;
    public String fileName;
    public int depth;
    public int file_status_id;
    public int approval_user_id;
    public String remark;

    @Override
    public String toString() {
        return fileName;
    }
}
