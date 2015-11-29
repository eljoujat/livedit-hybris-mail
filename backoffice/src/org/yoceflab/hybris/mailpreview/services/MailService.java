package org.yoceflab.hybris.mailpreview.services;

import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.MailInternalData;

import java.util.Collection;

/**
 * Created by eljoujat on 11/4/15.
 */
public interface MailService {

    Collection<MailInternalData> getAvailableMails();

}
