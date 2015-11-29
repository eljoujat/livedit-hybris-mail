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
package org.yoceflab.hybris.mailpreview.widgets.mailselector.controller;

import com.google.common.collect.ImmutableList;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import org.yoceflab.hybris.mailpreview.services.BusinessProcessService;
import org.yoceflab.hybris.mailpreview.services.MailService;
import org.yoceflab.hybris.mailpreview.widgets.AbstractImpersonationController;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.BusinessProcessInternalData;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.data.MailInternalData;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.renderer.BusinessProcessItemRenderer;
import org.yoceflab.hybris.mailpreview.widgets.mailselector.renderer.MailListItemRenderer;
import de.hybris.platform.commercesearchbackoffice.data.SiteData;
import de.hybris.platform.commerceservices.impersonation.ImpersonationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;

import java.util.Collection;


public class MailselectorController extends AbstractImpersonationController {

	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(MailselectorController.class.getName());
	private Textbox mailUid;
	private Listbox mailList;
	private Listbox bpList;
	private Textbox businessProcessCode;
	@WireVariable
	private MailService mailService;

	@WireVariable
	private BusinessProcessService businessProcessService;

	private MailInternalData currentMailData;
	private BusinessProcessInternalData currentBpData;
	protected static final String SOCKET_IN_CONTEXT = "context";

	@WireVariable
	private FlexibleSearchService flexibleSearchService;


	public void preInitialize(Component comp) {
		super.preInitialize(comp);
	}


	@Override
	public void initialize(final Component comp) {
		super.initialize(comp);
		this.mailList.setItemRenderer(this.getMailItemRenderer());
		this.bpList.setItemRenderer(getBpItemRenderer());
		comp.addEventListener("onCreate", new EventListener() {
			public void onEvent(Event event) throws Exception {
				updateMailListBox(new MailInternalData("cvname", "uid"), new BusinessProcessInternalData());
			}
		});
	}



	@ViewEvent(
			componentID = "mailList",
			eventName = "onSelect"
	)
	public void onMailSelectionChanged(SelectEvent<Listitem, MailInternalData> event) {
		Listitem selectionChangedItem = (Listitem)event.getReference();
		MailInternalData selectedMail = (MailInternalData)selectionChangedItem.getValue();
		if(this.isValidMail(selectedMail)) {
			this.currentMailData=selectedMail;
			//this.sendOutput("selectMail", this.clone(this.currentMailData));
			Collection<BusinessProcessInternalData> availableProcess = businessProcessService.getProcessByEmailUid(selectedMail.getUid());
			if(CollectionUtils.isNotEmpty(availableProcess)){
				BusinessProcessInternalData selectedBp=availableProcess.iterator().next();
				SimpleListModel bProcessModel = new SimpleListModel(ImmutableList.copyOf(availableProcess));
				this.bpList.setModel(bProcessModel);
				bProcessModel.setSelection(ImmutableList.of(availableProcess.iterator().next()));
				currentMailData.setBusinessOrderCode(selectedBp.getCode());
				this.sendOutput("selectMail", this.clone(currentMailData));
			}
		} else {
			LOG.info("Mail not Valid");
		}

	}

	@SocketEvent(
			socketId = "site"
	)
	public void handleNewSite(SiteData newSiteData) {
			Collection<MailInternalData> availableMails = this.getAvailableMails();
			SimpleListModel siteListModel = new SimpleListModel(ImmutableList.copyOf(availableMails));
			this.mailList.setModel(siteListModel);

	}


	@ViewEvent(
			componentID = "bpList",
			eventName = "onSelect"
	)
	public void onBpSelectionChanged(SelectEvent<Listitem, BusinessProcessInternalData> event) {
		Listitem selectionChangedItem = (Listitem)event.getReference();
		BusinessProcessInternalData selectedBp = (BusinessProcessInternalData)selectionChangedItem.getValue();
        this.currentMailData.setBusinessOrderCode(selectedBp.getCode());
		this.currentBpData=selectedBp;
		this.sendOutput("selectMail", this.clone(this.currentMailData));
	}


	protected boolean isValidMail(MailInternalData mail) {
		Collection<MailInternalData> availableMails = this.mailService.getAvailableMails();
		return availableMails.contains(mail);
	}

	protected Collection<MailInternalData> getAvailableMails() {

		final Collection<MailInternalData> availableMails =this.executeInContext(new ImpersonationService.Executor<Collection<MailInternalData>, RuntimeException>() {
			@Override
			public Collection<MailInternalData> execute() throws RuntimeException {
				return mailService.getAvailableMails();
			}
		});

		return availableMails;
	}

	protected Collection<BusinessProcessInternalData> getAvailableBprocess() {

		final Collection<BusinessProcessInternalData> availablProcess =this.executeInContext(new ImpersonationService.Executor<Collection<BusinessProcessInternalData>, RuntimeException>() {
			@Override
			public Collection<BusinessProcessInternalData> execute() throws RuntimeException {
				return businessProcessService.getAvailablesProcess();
			}
		});

		return availablProcess;
	}

	private void updateMailListBox(MailInternalData mailData,BusinessProcessInternalData bpData) {
		Collection<MailInternalData> availableMails = this.getAvailableMails();
		SimpleListModel siteListModel = new SimpleListModel(ImmutableList.copyOf(availableMails));
		this.mailList.setModel(siteListModel);
		siteListModel.setSelection(ImmutableList.of(mailData));


	}

	protected MailListItemRenderer getMailItemRenderer() {
		return new MailListItemRenderer();
	}

	protected BusinessProcessItemRenderer getBpItemRenderer() {
		return new BusinessProcessItemRenderer();
	}

	private MailInternalData clone(MailInternalData mailData) {
		MailInternalData copy = new MailInternalData();
		copy.setUid(mailData.getUid());
		copy.setFrontendTemplateName(mailData.getFrontendTemplateName());
		copy.setCatalogVersionName(mailData.getCatalogVersionName());
		copy.setBusinessOrderCode(mailData.getBusinessOrderCode());

		return copy;
	}
}

