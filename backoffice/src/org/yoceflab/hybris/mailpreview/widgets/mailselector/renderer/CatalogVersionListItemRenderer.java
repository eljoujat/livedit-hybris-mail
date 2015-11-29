package org.yoceflab.hybris.mailpreview.widgets.mailselector.renderer;

import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.CatalogInternalData;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Created by eljoujat on 11/4/15.
 */
public class CatalogVersionListItemRenderer implements ListitemRenderer<CatalogInternalData> {
    public CatalogVersionListItemRenderer() {
    }

    public void render(Listitem listitem, CatalogInternalData catalogData, int index) throws Exception {
        listitem.setValue(catalogData);
        listitem.setLabel(catalogData.getCatalogVersionName());
    }
}
