package com.qiyi.apilib.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BaseEntity implements Serializable {
    /**
     * 服务器返回码，100000=成功
     */
    @SerializedName("code")
    public int code;

}
