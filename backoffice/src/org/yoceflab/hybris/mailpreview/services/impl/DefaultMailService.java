package org.yoceflab.hybris.mailpreview.services.impl;

import org.yoceflab.hybris.mailpreview.services.MailService;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.MailInternalData;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.*;

/**
 * Created by eljoujat on 11/4/15.
 */
public class DefaultMailService implements MailService {


    FlexibleSearchService flexibleSearchService;

    @Override
    public Collection<MailInternalData> getAvailableMails() {

// search for products. A CategoryProductRelation must exist where the given category is the source and the product is the target.
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT {ep:").append(EmailPageModel.PK).append("} ");
        stringBuilder.append("FROM {").append(EmailPageModel._TYPECODE).append(" AS ep join catalogversion as cv on {ep.catalogVersion}={cv.pk}}  where {cv.version}='Online'");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(stringBuilder.toString());
        query.setNeedTotal(true);
        query.setResultClassList(Collections.singletonList(EmailPageModel.class));
        final SearchResult<EmailPageModel> result = flexibleSearchService.search(query);

        Collection<EmailPageModel> emailPageModels=result.getResult();

        return convertToInternalData(emailPageModels);
    }


    private MailInternalData convertToInternalData(EmailPageModel emailPageModel) {

        MailInternalData md= new MailInternalData();
        md.setCatalogVersionName(emailPageModel.getCatalogVersion().getCatalog().getId());
        md.setUid(emailPageModel.getUid());
        return md;
    }


    private Collection<MailInternalData> convertToInternalData(Collection<EmailPageModel> allMails) {
        ArrayList resultList = new ArrayList();
        Iterator var4 = allMails.iterator();

        while(var4.hasNext()) {
            EmailPageModel md = (EmailPageModel)var4.next();
            resultList.add(this.convertToInternalData(md));
        }

        return resultList;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
