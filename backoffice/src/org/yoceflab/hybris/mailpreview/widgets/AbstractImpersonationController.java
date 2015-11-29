/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2013 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 * 
 *  
 */
package org.yoceflab.hybris.mailpreview.widgets;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.util.DefaultWidgetController;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercesearchbackoffice.data.SiteData;
import de.hybris.platform.commerceservices.impersonation.ImpersonationContext;
import de.hybris.platform.commerceservices.impersonation.ImpersonationService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;


/**
 * Abstract widget controller that integrates with {@link ImpersonationService} to allow controllers to call business
 * logic in the impersonation context suitable for the instore application. To do so, extend from this class and call
 * the facade methods within
 * {@link #executeInContext(ImpersonationService.Executor)} method.
 */
public abstract class AbstractImpersonationController extends DefaultWidgetController
{

	private static final Logger LOG = Logger.getLogger(AbstractImpersonationController.class);

	private SiteData siteData;

	@WireVariable
	private CatalogVersionService catalogVersionService;
	@WireVariable
	protected ImpersonationService impersonationService;
	@WireVariable
	protected UserService userService;
	@WireVariable
	protected SessionService sessionService;
	@WireVariable
	protected BaseSiteService baseSiteService;
	@WireVariable
	protected SearchRestrictionService searchRestrictionService;
	@WireVariable
	protected CommonI18NService commonI18NService;
	@WireVariable
	protected CockpitLocaleService cockpitLocaleService;


	private ImpersonationContext localContext;


	/**
	 * Creates {@link ImpersonationContext} suitable for store chain application. The context elements:
	 * <ul>
	 * <li>user</li>
	 * <li>catalogVersions</li>
	 * <li>language</li>
	 * <li>currency</li>
	 * <li>site</li>
	 * </ul>
	 * disabled).
	 * 
	 * @return {@link ImpersonationContext}
	 */
	protected ImpersonationContext createImpersonationContext()
	{

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating new impersonation context for controller : " + this.getClass());
			}
			final UserModel user = userService.getCurrentUser();
			final ImpersonationContext newContext = sessionService.<ImpersonationContext> executeInLocalView(
					new SessionExecutionBody()
					{
						@Override
						public ImpersonationContext execute()
						{
							searchRestrictionService.disableSearchRestrictions();

							final ImpersonationContext newContext = new ImpersonationContext();
							final CMSSiteModel baseSite = (CMSSiteModel) baseSiteService.getBaseSiteForUID("ecom");
							CatalogVersionModel catalogVersion = AbstractImpersonationController.this.getCurrentCatalogVersion();
							newContext.setCatalogVersions(Collections.singleton(catalogVersion));
							newContext.setUser(user);
							newContext.setSite(baseSite);


							return newContext;
						}
					}, user);
			newContext.setUser(user);
		return newContext;
	}


	protected  ImpersonationContext getLocalContext(){
		if (localContext == null)
		{
			localContext = createImpersonationContext();
		}
		return localContext;

	}

	/**
	 * Computes catalogVersions for impersonated execution. This aggregates the product and content catalog version.
	 * 
	 * @return {@link Collection} of {@link CatalogVersionModel}
	 */
	private Collection<CatalogVersionModel> getContextCatalogVersions()
	{

		return Collections.emptyList();
	}

	@SocketEvent(
			socketId = "site"
	)
	public void updateContext(SiteData newSiteData){
		this.siteData = newSiteData;
		localContext=createImpersonationContext();
	}


	protected CatalogVersionModel getCurrentCatalogVersion() {
		return siteData == null?null:this.catalogVersionService.getCatalogVersion(siteData.getCatalogId(), siteData.getCatalogVersionName());
	}
	/**
	 * Computes language for impersonated execution
	 * 
	 * @param user
	 * @param baseSite
	 * @param baseStore
	 * @return {@link LanguageModel}
	 */
	protected LanguageModel getContextLanguage(final UserModel user, final BaseSiteModel baseSite, final BaseStoreModel baseStore)
	{
		LanguageModel language = null;
		final Locale currentLocale = cockpitLocaleService.getCurrentLocale();
		if (currentLocale == null)
		{
			LOG.debug("Current locale is null, falling back to users sessionLanguage");
			language = user.getSessionLanguage();
		}
		else
		{
			try
			{
				language = commonI18NService.getLanguage(currentLocale.getLanguage());
			}
			catch (final UnknownIdentifierException e)
			{
				LOG.debug("Could not find language for current locale, falling back to users sessionLanguage");
				language = user.getSessionLanguage();
			}
		}

		if (language == null)
		{
			language = baseSite.getDefaultLanguage();
		}
		if (language == null)
		{
			language = baseStore.getDefaultLanguage();
		}
		return language;
	}

	/**
	 * Computes currency for impersonated execution
	 * 
	 * @param user
	 * @param baseStore
	 * @return {@link CurrencyModel}
	 */
	protected CurrencyModel getContextCurrency(final UserModel user, final BaseStoreModel baseStore)
	{
		CurrencyModel currency = user.getSessionCurrency();
		if (currency == null)
		{
			currency = baseStore.getDefaultCurrency();
		}
		return currency;
	}

	/**
	 * Executes the given Executor in the context created by {@link #createImpersonationContext()}.
	 * 
	 * @param <R>
	 *           - Executor's return type
	 * @param <E>
	 *           - Exception thrown by executor, if any (Use ImpersonationService.Nothing if your executor doesn't throw
	 *           a checked exception.)
	 * @param executor
	 *           - Executor to run
	 * @return <R> result of execution.
	 * @throws E
	 */
	protected final <R, E extends Throwable> R executeInContext(final ImpersonationService.Executor<R, E> executor) throws E
	{
		if (impersonationService == null)
		{
			return executor.execute();
		}
		else
		{
			return impersonationService.executeInContext(getLocalContext(), executor);
		}
	}
}
