package com.highcom.todolog.ui.themelist;

public class ThemeColorItem {
    private String mColorName;
    private Integer mColorCode;

    public ThemeColorItem() {

    }

    public ThemeColorItem(String colorName, Integer colorCode) {
        mColorName = colorName;
        mColorCode = colorCode;
    }

    public String getColorName() {
        return mColorName;
    }

    public Integer getColorCode() {
        return mColorCode;
    }
}
