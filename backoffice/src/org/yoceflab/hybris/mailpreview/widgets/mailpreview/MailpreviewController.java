/*
 * [y] hybris Platform
 * 
 * Copyright (c) 2000-2012 hybris AG
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
package org.yoceflab.hybris.mailpreview.widgets.mailpreview;

import com.hybris.cockpitng.annotations.SocketEvent;
import org.yoceflab.hybris.mailpreview.MailPreviewEmailContextFactory;
import org.yoceflab.hybris.mailpreview.data.VmEmailTemplateData;
import org.yoceflab.hybris.mailpreview.widgets.AbstractImpersonationController;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.BusinessProcessInternalData;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.MailInternalData;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.renderer.VeloctityRenderer;
import de.hybris.platform.acceleratorservices.email.impl.DefaultCMSEmailPageService;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageTemplateModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.commerceservices.impersonation.ImpersonationService;
import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.commons.renderer.impl.DefaultRendererService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.internal.model.impl.DefaultModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;

import java.io.StringWriter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MailpreviewController extends AbstractImpersonationController
{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(MailpreviewController.class.getName());
	private Html html;
	private Label label;
	protected static final String SOCKET_IN_CONTEXT = "context";

	@WireVariable
	private DefaultRendererService rendererService;

	@WireVariable
	private FlexibleSearchService flexibleSearchService;

	@WireVariable
	private DefaultCMSEmailPageService cMSEmailPageService;
	@WireVariable
	private MailPreviewEmailContextFactory emailContextFactory;
	@WireVariable
	private DefaultModelService modelService;
	@WireVariable
	private VeloctityRenderer velocityTemplateRenderer;

	private MailInternalData selectedMail;
	private BusinessProcessInternalData selectedBp;

	private RendererTemplateModel currentBodyRenderTemplate;
	private  AbstractEmailContext<BusinessProcessModel> currentEmailContext;



	@SocketEvent(
			socketId = "mail"
	)
	public void displaySearchresult(MailInternalData mailData) {

		FlexibleSearchQuery query=new FlexibleSearchQuery("Select {pk} from {EmailPage} where {uid}='" + mailData.getUid() + "'");
		SearchResult<EmailPageModel> searhResult=flexibleSearchService.search(query);
		final EmailPageModel emailPageModel=searhResult.getResult().get(0);

		FlexibleSearchQuery querybm=new FlexibleSearchQuery("Select {pk} from {BusinessProcess } where {code}='" + mailData.getBusinessOrderCode() + "'");
		SearchResult<BusinessProcessModel> searhResultBs=flexibleSearchService.search(querybm);
		final BusinessProcessModel businessProcessModel=searhResultBs.getResult().get(0);

		final EmailPageTemplateModel emailPageTemplateModel = (EmailPageTemplateModel) emailPageModel.getMasterTemplate();
		currentBodyRenderTemplate = emailPageTemplateModel.getHtmlTemplate();
		currentEmailContext =this.executeInContext(new ImpersonationService.Executor<AbstractEmailContext<BusinessProcessModel>, RuntimeException>() {
			@Override
			public AbstractEmailContext<BusinessProcessModel> execute() throws RuntimeException {
				return emailContextFactory.create(businessProcessModel,
						emailPageModel, currentBodyRenderTemplate);
			}
		});

		final StringWriter body = new StringWriter();
		rendererService.render(currentBodyRenderTemplate, currentEmailContext, body);
		html.setContent(body.toString());
		//label.setValue(emailPageTemplateModel.getHtmlTerendererServicemplate().getTemplateScript());
		VmEmailTemplateData vmData=new VmEmailTemplateData();
		vmData.setBody(emailPageTemplateModel.getHtmlTemplate().getTemplateScript());
		this.sendOutput("selectVmTemplate", vmData);

	}

	@SocketEvent(
			socketId = "changevmTemplate"
	)
	public void updatePreviewMail(VmEmailTemplateData vmData) {

		final StringWriter body = new StringWriter();
		currentBodyRenderTemplate.setTemplateScript(vmData.getBody());
		velocityTemplateRenderer.render(currentBodyRenderTemplate, currentEmailContext, body);
		html.setContent(body.toString());

	}


	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);


	}
}
