package org.yoceflab.hybris.mailpreview.widgets.mailselector.renderer;

import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.MailInternalData;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Created by eljoujat on 11/4/15.
 */
public class MailListItemRenderer implements ListitemRenderer<MailInternalData> {

    public MailListItemRenderer(){

    }


    public void render(Listitem listitem, MailInternalData mailData, int index) throws Exception {
        listitem.setValue(mailData);
        listitem.setLabel(mailData.getUid());
    }

}
