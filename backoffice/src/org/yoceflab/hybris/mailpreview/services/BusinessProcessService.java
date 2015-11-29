package org.yoceflab.hybris.mailpreview.services;

import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.BusinessProcessInternalData;

import java.util.Collection;

/**
 * Created by eljoujat on 11/8/15.
 */
public interface BusinessProcessService {

    Collection<BusinessProcessInternalData> getAvailablesProcess();

    public Collection<BusinessProcessInternalData> getProcessByEmailUid(String emailPageUid);
}
