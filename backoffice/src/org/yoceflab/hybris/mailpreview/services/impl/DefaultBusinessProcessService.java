package org.yoceflab.hybris.mailpreview.services.impl;

import org.yoceflab.hybris.mailpreview.services.BusinessProcessService;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.BusinessProcessInternalData;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.*;

/**
 * Created by eljoujat on 11/8/15.
 */
public class DefaultBusinessProcessService implements BusinessProcessService {

    FlexibleSearchService flexibleSearchService;

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }


    @Override
    public Collection<BusinessProcessInternalData> getAvailablesProcess() {

        // search for products. A CategoryProductRelation must exist where the given category is the source and the product is the target.
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT {bp:").append(BusinessProcessModel.PK).append("} ");
        stringBuilder.append("FROM {").append(BusinessProcessModel._TYPECODE).append(" AS bp } ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(stringBuilder.toString());
        query.setNeedTotal(true);
        query.setResultClassList(Collections.singletonList(BusinessProcessModel.class));
        final SearchResult<BusinessProcessModel> result = flexibleSearchService.search(query);
        Collection<BusinessProcessModel> bpModels=result.getResult();
        return convertToInternalData(bpModels);

    }


    @Override
    public Collection<BusinessProcessInternalData> getProcessByEmailUid(String emailPageUid) {

        // search for products. A CategoryProductRelation must exist where the given category is the source and the product is the target.
        final StringBuilder stringBuilder = new StringBuilder();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("emailPageUid", emailPageUid);

        /*

        select {bp.pk}
from {StoreFrontCustomerProcess as bp join ProcessState as ps on {bp.state}={ps.pk}} where  {ps.code}='SUCCEEDED'
         */
        stringBuilder.append("SELECT {bp:").append(BusinessProcessModel.PK).append("} ");
        stringBuilder.append("FROM {").append(BusinessProcessModel._TYPECODE).append(" AS bp join ProcessState as ps on {bp.state}={ps.pk}} where {ps.code}='SUCCEEDED'  AND LOWER({code}) LIKE '%"+emailPageUid.toLowerCase()+"%' ORDER BY  {bp.creationtime} DESC");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(stringBuilder.toString());
        query.setNeedTotal(true);
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BusinessProcessModel.class));
        final SearchResult<BusinessProcessModel> result = flexibleSearchService.search(query);
        Collection<BusinessProcessModel> bpModels=result.getResult();
        return convertToInternalData(bpModels);

    }





    private BusinessProcessInternalData convertToInternalData(BusinessProcessModel bpModel) {

        BusinessProcessInternalData bpd= new BusinessProcessInternalData();
        bpd.setCode(bpModel.getCode());
        return bpd;
    }


    private Collection<BusinessProcessInternalData> convertToInternalData(Collection<BusinessProcessModel> allBpModel) {
        ArrayList resultList = new ArrayList();
        Iterator var4 = allBpModel.iterator();

        while(var4.hasNext()) {
            BusinessProcessModel bpm = (BusinessProcessModel)var4.next();
            resultList.add(this.convertToInternalData(bpm));
        }

        return resultList;
    }
}
