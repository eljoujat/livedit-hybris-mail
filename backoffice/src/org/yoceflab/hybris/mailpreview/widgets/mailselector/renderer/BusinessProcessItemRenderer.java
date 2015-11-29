package org.yoceflab.hybris.mailpreview.widgets.mailselector.renderer;

import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.BusinessProcessInternalData;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Created by eljoujat on 11/4/15.
 */
public class BusinessProcessItemRenderer implements ListitemRenderer<BusinessProcessInternalData> {

    public BusinessProcessItemRenderer(){

    }


    public void render(Listitem listitem, BusinessProcessInternalData bpData, int index) throws Exception {
        listitem.setValue(bpData);
        listitem.setLabel(bpData.getCode());
    }

}
