/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.] Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gtri.txdps.dc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractDataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;

import org.opensaml.security.httpclient.impl.SecurityEnhancedHttpClientSupport;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import java.net.URLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Date;
import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;



/**
 * Data connector implementation that returns staticly defined attributes.
 */
public class IIRDataConnector extends AbstractDataConnector {

    /** Log4j logger. */
    @NonnullAfterInit private final Logger log =  LoggerFactory.getLogger(IIRDataConnector.class);

    /** Source Data. */
//    private Map<String, BaseAttribute> attributes;

    @NonnullAfterInit private String queryUrl;
    @NonnullAfterInit private String emailAttributeId;
    @NonnullAfterInit private String attrName;

    /**
     * Constructor.
     * 
     * @param fileAttributes attributes that configure this data connector
     */
    public IIRDataConnector() { 
    }

    /**
      * Set the attr
      * 
      * @param attr what to set.
      */
    public void setAttrName(@Nullable String attr) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        attrName = StringSupport.trimOrNull(attr);
    }
    
    /**
      * Get the attr
      * 
      * @return the attr.
      */
    @NonnullAfterInit public String getAttrName() {
        return attrName;
    }
    /**
      * Set the url
      * 
      * @param url what to set.
      */
    public void setQueryUrl(@Nullable String url) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        queryUrl = StringSupport.trimOrNull(url);
    }
    
    /**
      * Get the url
      * 
      * @return the url.
      */
    @NonnullAfterInit public String getQueryUrl() {
        return queryUrl;
    }

    /**
      * Set the email
      * 
      * @param email what to set.
      */
    public void setUidAttribute(@Nullable String email) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        emailAttributeId = StringSupport.trimOrNull(email);
    }
    
    /**
      * Get the email
      * 
      * @return the email.
      */
    @NonnullAfterInit public String getEmailAttribute() {
        return emailAttributeId;
    }

    private String getPrincipal (
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) {

        final Map<String,List<IdPAttributeValue<?>>> dependencyAttributes =
                PluginDependencySupport.getAllAttributeValues(workContext, getDependencies());


        for (final Entry<String,List<IdPAttributeValue<?>>> dependencyAttribute : dependencyAttributes.entrySet()) {
            log.debug("Adding dependent attribute '{}' with the following values to the connector context: {}",
                      dependencyAttribute.getKey(), dependencyAttribute.getValue());
              if ( dependencyAttribute.getKey() == emailAttributeId ) {
                 String principalObjString = dependencyAttribute.getValue().toString();
                 int start = principalObjString.indexOf ('=');
                 int end   = principalObjString.indexOf ('}');
                 return principalObjString.substring (start + 1, end);
              }
        }

        return null;
    }


    /** {@inheritDoc} */
    @Override
    @Nonnull protected Map<String, IdPAttribute> doDataConnectorResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        Constraint.isNotNull(resolutionContext, "AttributeResolutionContext cannot be null");
        Constraint.isNotNull(workContext, "AttributeResolverWorkContext cannot be null");

        String strPrincipal = getPrincipal (resolutionContext, workContext);

        //We want to cleanup principals that are DNs
        if ( null == strPrincipal )
        {
           log.error ("Failed to identify the user's email address.");
           throw new ResolutionException("Unique principal not identified.");
        } 

        String jsonReq = "{ 'email' : '" + strPrincipal + "' }";

        log.debug ("Sending JSON Request (" + jsonReq + ") to URL (" + queryUrl + ")" );

        Map<String, IdPAttribute> outputAttr = new HashMap<String, IdPAttribute>(1); 

        CloseableHttpClient myclient = HttpClientBuilder.create().setSSLSocketFactory(SecurityEnhancedHttpClientSupport.buildTLSSocketFactory(false, false)).build();

        HttpPost myPost = new HttpPost(queryUrl);
        EntityBuilder builder = EntityBuilder.create();

        myPost.setEntity (builder.setText(jsonReq).build());
        myPost.setHeader ("Accept", "application/json");
        myPost.setHeader ("Content-type", "application/json");

        try {
          CloseableHttpResponse myResp = myclient.execute(myPost);
          String resp = EntityUtils.toString (myResp.getEntity());

          JSONObject result = new JSONObject (resp);

          Boolean Certification = result.getBoolean ("completed");

          myResp.close();

          final IdPAttribute tmpAttr = new IdPAttribute (attrName);
          if ( Certification ) {
            tmpAttr.setValues (Collections.singletonList(StringAttributeValue.valueOf("true")));
            outputAttr.put (tmpAttr.getId(), tmpAttr);
          } else {
            tmpAttr.setValues (Collections.singletonList(StringAttributeValue.valueOf("false")));
            outputAttr.put (tmpAttr.getId(), tmpAttr);
          }
       } catch (ClientProtocolException e) {
           log.error ("Client Protocol Exception when communicating with: " + queryUrl);
        } catch (IOException ie) {
           log.error ("I/O Error while reading from URL: " + queryUrl);
        } catch (Exception e) {
           log.error ("Unknown exception while querying webservice: " + e);
        } 

        return outputAttr;
    }

    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == attrName) {
            throw new ComponentInitializationException(getLogPrefix() + " No attribute name set up.");
        }
        if (null == queryUrl) {
            throw new ComponentInitializationException(getLogPrefix() + " No query url set up.");
        }
        if (null == emailAttributeId) {
            throw new ComponentInitializationException(getLogPrefix() + " No email attribute set up.");
        }
    }

}
