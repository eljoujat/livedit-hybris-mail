package org.yoceflab.hybris.mailpreview.widgets.mailselector.data;

import com.google.common.base.Objects;
import de.hybris.platform.commercesearchbackoffice.data.SiteData;

/**
 * Created by eljoujat on 11/4/15.
 */
public class CatalogInternalData {
    private String catalogVersionName;
    private String catalogId;
    private Boolean active;

    public CatalogInternalData() {
    }

    public CatalogInternalData(SiteData siteData) {
        this.catalogId = siteData.getCatalogId();
        this.catalogVersionName = siteData.getCatalogVersionName();
        this.active = siteData.getActive();
    }

    public String getCatalogVersionName() {
        return this.catalogVersionName;
    }

    public void setCatalogVersionName(String catalogVersionName) {
        this.catalogVersionName = catalogVersionName;
    }

    public String getCatalogId() {
        return this.catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this.catalogVersionName, this.catalogId});
    }

    public boolean equals(Object object) {
        if(object instanceof CatalogInternalData) {
            CatalogInternalData that = (CatalogInternalData)object;
            return Objects.equal(this.catalogVersionName, that.catalogVersionName) && Objects.equal(this.catalogId, that.catalogId);
        } else {
            return false;
        }
    }

    public String toString() {
        return Objects.toStringHelper(this).add("catalogVersionName", this.catalogVersionName).add("catalogId", this.catalogId).add("active", this.active).toString();
    }
}
