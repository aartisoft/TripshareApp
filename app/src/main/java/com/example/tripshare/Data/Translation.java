package com.example.tripshare.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Translation {
    @SerializedName("translatedText")
    @Expose
    private String translatedText;

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
