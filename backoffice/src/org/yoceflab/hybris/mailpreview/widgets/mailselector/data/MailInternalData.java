package org.yoceflab.hybris.mailpreview.widgets.mailselector.data;

/**
 * Created by eljoujat on 11/4/15.
 */
public class MailInternalData {

    private String catalogVersionName;
    private String uid;
    private  String frontendTemplateName;
    private String businessOrderCode;

    public MailInternalData(){

    }

    public MailInternalData(String catalogVersionName, String uid) {
        this.catalogVersionName = catalogVersionName;
        this.uid = uid;
    }



    public String getCatalogVersionName() {
        return catalogVersionName;
    }

    public void setCatalogVersionName(String catalogVersionName) {
        this.catalogVersionName = catalogVersionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MailInternalData that = (MailInternalData) o;

        if (!catalogVersionName.equals(that.catalogVersionName)) return false;
        return uid.equals(that.uid);

    }

    public String getFrontendTemplateName() {
        return frontendTemplateName;
    }

    public void setFrontendTemplateName(String frontendTemplateName) {
        this.frontendTemplateName = frontendTemplateName;
    }

    @Override
    public int hashCode() {
        int result = catalogVersionName.hashCode();
        result = 31 * result + uid.hashCode();
        return result;
    }

    public String getUid() {
        return uid;

    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getBusinessOrderCode() {
        return businessOrderCode;
    }

    public void setBusinessOrderCode(String businessOrderCode) {
        this.businessOrderCode = businessOrderCode;
    }
}
