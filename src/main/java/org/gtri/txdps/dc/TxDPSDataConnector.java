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

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;


/**
 * Data connector implementation that returns staticly defined attributes.
 */
public class TxDPSDataConnector extends AbstractDataConnector {

    /** Log4j logger. */
    @NonnullAfterInit private final Logger log =  LoggerFactory.getLogger(TxDPSDataConnector.class);

    /** Source Data. */
//    private Map<String, BaseAttribute> attributes;

    @NonnullAfterInit private String queryURL;
    @NonnullAfterInit private String uidAttributeId;
    @NonnullAfterInit private String serviceAccountCredential;
    @NonnullAfterInit private String serviceAccountUser;

    /**
     * Constructor.
     * 
     * @param fileAttributes attributes that configure this data connector
     */
    public TxDPSDataConnector() { 
       // String pathUserMetadata, String uidAttrName) {
       // pathToUserAttributeFiles = pathUserMetadata;
       // uidAttributeId           = uidAttrName;
    }

    /**
      * Set the url
      * 
      * @param url what to set.
      */
    public void setQueryUrl(@Nullable String url) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        queryURL = StringSupport.trimOrNull(url);
    }
    
    /**
      * Get the url
      * 
      * @return the url.
      */
    @NonnullAfterInit public String getQueryUrl() {
        return queryURL;
    }

    /**
      * Set the uid
      * 
      * @param uid what to set.
      */
    public void setUidAttribute(@Nullable String uid) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        uidAttributeId = StringSupport.trimOrNull(uid);
    }
    
    /**
      * Get the uid
      * 
      * @return the uid.
      */
    @NonnullAfterInit public String getUidAttribute() {
        return uidAttributeId;
    }
    /**
      * Set the credential
      * 
      * @param credential what to set.
      */
    public void setServiceAccountCredential(@Nullable String cred) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        serviceAccountCredential = StringSupport.trimOrNull(cred);
    }
    
    /**
      * Get the credential
      * 
      * @return the credential.
      */
    @NonnullAfterInit public String getServiceAccountCredential() {
        return serviceAccountCredential;
    }
 
    /**
      * Set the user.
      * 
      * @param user
      */
    public void setServiceAccountUser(@Nullable String user) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        serviceAccountUser = StringSupport.trimOrNull(user);
    }
    
    /**
      * Get the user.
      * 
      * @return the user.
      */
    @NonnullAfterInit public String getServiceAccountUser() {
        return serviceAccountUser;
    }

    private String getPrincipal (
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) {

        final Map<String,List<IdPAttributeValue<?>>> dependencyAttributes =
                PluginDependencySupport.getAllAttributeValues(workContext, getDependencies());


        for (final Entry<String,List<IdPAttributeValue<?>>> dependencyAttribute : dependencyAttributes.entrySet()) {
            log.debug("Adding dependent attribute '{}' with the following values to the connector context: {}",
                      dependencyAttribute.getKey(), dependencyAttribute.getValue());
              if ( dependencyAttribute.getKey() == uidAttributeId ) {
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
           log.error ("Failed to identify the principal");
           throw new ResolutionException("Unique principal not identified.");
        } 

        String urlQuery = queryURL + strPrincipal;
        log.debug ("Using Service Acount (" + serviceAccountUser  + ")& Credential (" + serviceAccountCredential + ") Invoking query for: " + urlQuery);

        Map<String, IdPAttribute> outputAttr = new HashMap<String, IdPAttribute>(1); 

	{
           // Debug Example - Insert Real Code Here and set the return value from the server as strResults before calling ParseXml;
           String strResults = "<ROLES><NQUIRY /></ROLES>";

           // TBD
        }            

        return outputAttr;
    }

    private IdPAttributeValue<String> ParseXmlResponse (String xmlResults) {

       StringAttributeValue attrVal = null;

       log.debug("Parsing XML Response: " + xmlResults);

       try {
          StringReader           xmlReader = new StringReader(xmlResults);
          DocumentBuilderFactory dbf       = DocumentBuilderFactory.newInstance();
          DocumentBuilder        db        = dbf.newDocumentBuilder();
          Document               doc       = db.parse(new InputSource(xmlReader));


          Element root = doc.getDocumentElement();
          NodeList nl  = root.getChildNodes ();
          if (nl != null && nl.getLength() > 0) {
            log.debug("Found ROLE Root Element: " + nl);
             for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element)nl.item(i);
                attrVal = new StringAttributeValue(el.getNodeName());
             }
          }
          else
          {
            log.debug("Node List Empty for: " + root);
          }

       } catch (Exception e) {
          log.error("Attribute Resolution Failure Parsing DPS Query Response: " + xmlResults);
       }

       return attrVal;
    }


    /** {@inheritDoc} */
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == queryURL) {
            throw new ComponentInitializationException(getLogPrefix() + " No query url set up.");
        }
        if (null == uidAttributeId) {
            throw new ComponentInitializationException(getLogPrefix() + " No uid attribute set up.");
        }
        if (null == serviceAccountCredential) {
            throw new ComponentInitializationException(getLogPrefix() + " No service account credential set up.");
        }
        if (null == serviceAccountUser) {
            throw new ComponentInitializationException(getLogPrefix() + " No service account user set up.");
        }
    }

}
