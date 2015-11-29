package org.yoceflab.hybris.mailpreview.widgets.mailselector.data;

/**
 * Created by eljoujat on 11/4/15.
 */
public class BusinessProcessInternalData {

    private String code;



    public BusinessProcessInternalData(){

    }

    public BusinessProcessInternalData(String catalogVersionName, String code) {
        this.code = code;
    }




    public String getCode() {
        return code;

    }

    public void setCode(String code) {
        this.code = code;
    }
}
