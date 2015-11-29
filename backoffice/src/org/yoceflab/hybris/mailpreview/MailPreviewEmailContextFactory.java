/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package org.yoceflab.hybris.mailpreview;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.acceleratorservices.process.email.context.EmailContextFactory;
import de.hybris.platform.acceleratorservices.process.strategies.EmailTemplateTranslationStrategy;
import de.hybris.platform.acceleratorservices.process.strategies.ProcessContextResolutionStrategy;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import de.hybris.platform.cms2.model.contents.contentslot.ContentSlotModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.data.ContentSlotData;
import de.hybris.platform.cms2.servicelayer.services.CMSComponentService;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.commons.renderer.RendererService;
import de.hybris.platform.commons.renderer.daos.RendererTemplateDao;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;


/**
 * Default factory used to create the velocity context for rendering emails
 */
public class MailPreviewEmailContextFactory implements EmailContextFactory<BusinessProcessModel>
{
	private static final Logger LOG = Logger.getLogger(MailPreviewEmailContextFactory.class);

	private CMSPageService cmsPageService;
	private CMSComponentService cmsComponentService;
	private RendererTemplateDao rendererTemplateDao;
	private RendererService rendererService;
	private ModelService modelService;
	private TypeService typeService;
	private ProcessContextResolutionStrategy<CMSSiteModel> contextResolutionStrategy;
	private Map<String, String> emailContextVariables;
	private SiteBaseUrlResolutionService siteBaseUrlResolutionService;
	private EmailTemplateTranslationStrategy emailTemplateTranslationStrategy;

	@Override
	public AbstractEmailContext<BusinessProcessModel> create(final BusinessProcessModel businessProcessModel,
			final EmailPageModel emailPageModel, final RendererTemplateModel renderTemplate) throws RuntimeException
	{
		final AbstractEmailContext<BusinessProcessModel> emailContext = resolveEmailContext(renderTemplate);
		emailContext.init(businessProcessModel, emailPageModel);
		renderCMSSlotsIntoEmailContext(emailContext, emailPageModel, businessProcessModel);

		// parse and populate the variable at the end
		parseVariablesIntoEmailContext(emailContext);

		final String languageIso = emailContext.getEmailLanguage() == null ? null : emailContext.getEmailLanguage().getIsocode();
		//Render translated messages from the email message resource bundles into the email context.
		emailContext.setMessages(getEmailTemplateTranslationStrategy().translateMessagesForTemplate(renderTemplate, languageIso));

		return emailContext;
	}

	protected <T extends AbstractEmailContext<BusinessProcessModel>> T resolveEmailContext(
			final RendererTemplateModel renderTemplate) throws RuntimeException
	{
		try
		{
			final Class<T> contextClass = (Class<T>) Class.forName(renderTemplate.getContextClass());
			final Map<String, T> emailContexts = getApplicationContext().getBeansOfType(contextClass);
			if (MapUtils.isNotEmpty(emailContexts))
			{
				return emailContexts.entrySet().iterator().next().getValue();
			}
			else
			{
				throw new RuntimeException("Cannot find bean in application context for context class [" + contextClass + "]");
			}
		}
		catch (final ClassNotFoundException e)
		{
			LOG.error("failed to create email context", e);
			throw new RuntimeException("Cannot find email context class", e);
		}
	}

	protected ApplicationContext getApplicationContext()
	{
		return Registry.getGlobalApplicationContext();
	}

	protected void renderCMSSlotsIntoEmailContext(final AbstractEmailContext<BusinessProcessModel> emailContext,
			final EmailPageModel emailPageModel, final BusinessProcessModel businessProcessModel)
	{
		final Map<String, String> cmsSlotContents = new HashMap<String, String>();

		for (final ContentSlotData contentSlotData : getCmsPageService().getContentSlotsForPage(emailPageModel))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Starting to prodess Content Slot: " + contentSlotData.getName() + "...");
			}

			final String contentPosition = contentSlotData.getPosition();
			final String renderedComponent = renderComponents(contentSlotData.getContentSlot(), emailContext, businessProcessModel);
			cmsSlotContents.put(contentPosition, renderedComponent);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Content Slot Position: " + contentPosition);
				LOG.debug("Renedered Component: " + renderedComponent);

				LOG.debug("Finished Processing Content Slot: " + contentSlotData.getName());
			}
		}
		emailContext.setCmsSlotContents(cmsSlotContents);
	}

	protected String renderComponents(final ContentSlotModel contentSlotModel,
			final AbstractEmailContext<BusinessProcessModel> emailContext, final BusinessProcessModel businessProcessModel)
	{
		final StringWriter text = new StringWriter();
		for (final AbstractCMSComponentModel component : contentSlotModel.getCmsComponents())
		{
			final ComposedTypeModel componentType = getTypeService().getComposedTypeForClass(component.getClass());
			if (Boolean.TRUE.equals(component.getVisible())
					&& !getCmsComponentService().isComponentContainer(componentType.getCode()))
			{
				final String renderTemplateCode = resolveRendererTemplateForComponent(component, businessProcessModel);
				final List<RendererTemplateModel> results = getRendererTemplateDao().findRendererTemplatesByCode(renderTemplateCode);
				final RendererTemplateModel renderTemplate = results.isEmpty() ? null : results.get(0);
				final BaseSiteModel site = getContextResolutionStrategy().getCmsSite(businessProcessModel);
				if (renderTemplate != null)
				{
					if (LOG.isDebugEnabled())
					{
						LOG.debug("Using Render Template Code: " + renderTemplateCode);
					}

					final Map<String, Object> componentContext = new HashMap<String, Object>();
					componentContext.put("parentContext", emailContext);
					for (final String property : getCmsComponentService().getEditorProperties(component))
					{
						try
						{
							final Object value = modelService.getAttributeValue(component, property);
							componentContext.put(property, value);
						}
						catch (final AttributeNotSupportedException ignore)
						{
							// ignore
						}
					}
					//insert services for usage at jsp/vm page
					componentContext.put("urlResolutionService", getSiteBaseUrlResolutionService());
					//insert cms site
					componentContext.put("site", site);

					if (LOG.isDebugEnabled())
					{
						for (final Entry<String, Object> entry : componentContext.entrySet())
						{
							LOG.debug("Render template Context Data: " + entry.getKey() + "=" + entry.getValue());
						}
						final Object[] keys = emailContext.getKeys();
						if (keys != null)
						{
							for (final Object key : keys)
							{
								LOG.debug("Parent render template Context Data: " + key + "=" + emailContext.get(String.valueOf(key)));
							}
						}
					}

					getRendererService().render(renderTemplate, componentContext, text);
				}
				else
				{
					// Component won't get rendered in the emails.
					final String siteName = site == null ? null : site.getUid();

					LOG.error("Couldn't find render template for component [" + component.getUid() + "] of type ["
							+ componentType.getCode() + "] in slot [" + contentSlotModel.getUid() + "] for site [" + siteName
							+ "] during process [" + businessProcessModel + "]. Tried code [" + renderTemplateCode + "]");
				}
			}
		}
		return text.toString();
	}

	protected String resolveRendererTemplateForComponent(final AbstractCMSComponentModel component,
			final BusinessProcessModel businessProcessModel)
	{
		final BaseSiteModel site = getContextResolutionStrategy().getCmsSite(businessProcessModel);
		final ComposedTypeModel componentType = getTypeService().getComposedTypeForClass(component.getClass());

		return (site != null ? site.getUid() : "") + "-" + componentType.getCode() + "-template";
	}

	protected void parseVariablesIntoEmailContext(final AbstractEmailContext<BusinessProcessModel> emailContext)
	{
		final Map<String, String> variables = getEmailContextVariables();
		if (variables != null)
		{
			for (final Entry<String, String> entry : variables.entrySet())
			{
				final StringBuilder buffer = new StringBuilder();

				final StringTokenizer tokenizer = new StringTokenizer(entry.getValue(), "{}");
				while (tokenizer.hasMoreElements())
				{
					final String token = tokenizer.nextToken();
					if (emailContext.containsKey(token))
					{
						final Object tokenValue = emailContext.get(token);
						if (tokenValue != null)
						{
							buffer.append(tokenValue.toString());
						}
					}
					else
					{
						buffer.append(token);
					}
				}

				emailContext.put(entry.getKey(), buffer.toString());
			}
		}
	}

	protected CMSPageService getCmsPageService()
	{
		return cmsPageService;
	}

	@Required
	public void setCmsPageService(final CMSPageService cmsPageService)
	{
		this.cmsPageService = cmsPageService;
	}

	protected CMSComponentService getCmsComponentService()
	{
		return cmsComponentService;
	}

	@Required
	public void setCmsComponentService(final CMSComponentService cmsComponentService)
	{
		this.cmsComponentService = cmsComponentService;
	}

	protected RendererTemplateDao getRendererTemplateDao()
	{
		return rendererTemplateDao;
	}

	@Required
	public void setRendererTemplateDao(final RendererTemplateDao rendererTemplateDao)
	{
		this.rendererTemplateDao = rendererTemplateDao;
	}

	protected RendererService getRendererService()
	{
		return rendererService;
	}

	@Required
	public void setRendererService(final RendererService rendererService)
	{
		this.rendererService = rendererService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	protected ProcessContextResolutionStrategy<CMSSiteModel> getContextResolutionStrategy()
	{
		return contextResolutionStrategy;
	}

	@Required
	public void setContextResolutionStrategy(final ProcessContextResolutionStrategy<CMSSiteModel> contextResolutionStrategy)
	{
		this.contextResolutionStrategy = contextResolutionStrategy;
	}

	protected Map<String, String> getEmailContextVariables()
	{
		return emailContextVariables;
	}

	public void setEmailContextVariables(final Map<String, String> emailContextVariables)
	{
		this.emailContextVariables = emailContextVariables;
	}

	protected SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
	{
		return siteBaseUrlResolutionService;
	}

	@Required
	public void setSiteBaseUrlResolutionService(final SiteBaseUrlResolutionService siteBaseUrlResolutionService)
	{
		this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
	}

	protected EmailTemplateTranslationStrategy getEmailTemplateTranslationStrategy()
	{
		return emailTemplateTranslationStrategy;
	}

	@Required
	public void setEmailTemplateTranslationStrategy(final EmailTemplateTranslationStrategy emailTemplateTranslationStrategy)
	{
		this.emailTemplateTranslationStrategy = emailTemplateTranslationStrategy;
	}

}
