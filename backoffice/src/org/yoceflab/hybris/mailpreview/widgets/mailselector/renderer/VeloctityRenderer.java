package org.yoceflab.hybris.mailpreview.widgets.mailselector.renderer;

import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.commons.renderer.exceptions.RendererException;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Required;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by eljoujat on 11/22/15.
 */
public class VeloctityRenderer {

    private String contextName;

    public void render(final RendererTemplateModel template, final Object context, final Writer output)
    {
        Class clazz = null;

        try
        {
            clazz = Class.forName(template.getContextClass());
        }
        catch (final ClassNotFoundException e)
        {
            throw new RendererException("Cannot find class: " + template.getContextClass(), e);
        }

        InputStream inputStream = null;
        try
        {
            if ((context != null) && (!clazz.isAssignableFrom(context.getClass())))
            {
                throw new RendererException("The context class [" + context.getClass().getName() + "] is not correctly defined.");
            }
            final MediaModel content = template.getContent();
            if (content == null)
            {
                throw new RendererException("No content found for template " + template.getCode());
            }

            inputStream =  new ByteArrayInputStream(template.getTemplateScript().getBytes(StandardCharsets.UTF_8));

            writeToOutput(output, inputStream, context);
        }
        catch (final IOException e)
        {
            throw new RendererException("Problem during rendering", e);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public void writeToOutput(final Writer result, final InputStream inputStream, final Object context) throws IOException
    {
        final VelocityContext ctx = new VelocityContext();
        ctx.put(contextName, context);

        final Reader reader = new InputStreamReader(inputStream, "UTF-8");

        try
        {
            evaluate(result, ctx, reader);
            result.flush();
        }
        catch (final Exception e)
        {
            throw new RendererException("Problem with get velocity stream", e);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    protected void evaluate(final Writer result, final VelocityContext ctx, final Reader reader) throws IOException
    {
        Velocity.evaluate(ctx, result, getClass().getName(), reader);
    }

    /**
     * @param contextName
     *           the contextName to set
     */
    @Required
    public void setContextName(final String contextName)
    {
        this.contextName = contextName;
    }
}
