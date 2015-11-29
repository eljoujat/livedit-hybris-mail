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
package org.yoceflab.hybris.mailpreview.widgets.vmeditor.controller;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import org.yoceflab.hybris.mailpreview.data.VmEmailTemplateData;
import org.yoceflab.hybris.mailpreview.widgets.AbstractImpersonationController;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.*;


public class VmEditorController extends AbstractImpersonationController {

	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(VmEditorController.class.getName());
	private Textbox bodyScript;



	@SocketEvent(
			socketId = "vmTemplate"
	)

	public void displayVmTemplateCode(VmEmailTemplateData vmEmailTemplateData) {
		bodyScript.setValue(vmEmailTemplateData.getBody());
	}


	@ViewEvent(
			componentID = "bodyScriptCode",
			eventName = Events.ON_CHANGE
	)
	public void onVmEditorContentChanged(InputEvent event) {
		String chnagedVm = (String)event.getValue();
		VmEmailTemplateData vmData=new VmEmailTemplateData();
		vmData.setBody(chnagedVm);
		this.sendOutput("updateVmTemplate", vmData);
	}



	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);

	}
}

