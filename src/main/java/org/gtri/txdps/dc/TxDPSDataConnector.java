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
public class TxDPSDataConnector extends AbstractDataConnector {

    /** Log4j logger. */
    @NonnullAfterInit private final Logger log =  LoggerFactory.getLogger(TxDPSDataConnector.class);

    /** Source Data. */
//    private Map<String, BaseAttribute> attributes;

    @NonnullAfterInit private String queryUrl;
    @NonnullAfterInit private String uidAttributeId;
    @NonnullAfterInit private String serviceAccountCredential;
    @NonnullAfterInit private String serviceAccountUser;
    @NonnullAfterInit private String attrName;

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

        String urlQuery = queryUrl + strPrincipal;
        log.debug ("Populating Attribute (" + attrName + ") Using Service Account (" + serviceAccountUser  + ")& Credential (" + serviceAccountCredential + ") Invoking query for: " + urlQuery);

        Map<String, IdPAttribute> outputAttr = new HashMap<String, IdPAttribute>(1); 


        try {
          URL geturl = new URL(urlQuery);
          HttpURLConnection  connection = (HttpURLConnection) geturl.openConnection();
          String sig = sign (geturl, "GET", connection, serviceAccountCredential);
          connection.addRequestProperty("Authorization", "CJIS256 " + serviceAccountUser + ":" + sig);
          log.debug ("  Custom Signature = " + sig);
          connection.connect();
          InputStream is = connection.getInputStream();
          byte[] buf = new byte[1024];
          int num;
          log.debug ("  Custom Web Service Response Code = " + connection.getResponseCode());
          String strResults = null;
          while ( (num = is.read(buf)) > 0) {
            byte[] bytes = new byte[num];
            System.arraycopy(buf, 0, bytes, 0, num);
            strResults = new String(bytes);
            log.debug ("  Custom Web Service Response = " + strResults);
          }
          // Debug Example - Insert Real Code Here and set the return value from the server as strResults before calling ParseXml;
          // String strResults = "<ROLES><NQUIRY/><TEST2/></ROLES>";

          if ( strResults != null ) {
            final IdPAttribute tmpAttr = new IdPAttribute (attrName);
            tmpAttr.setValues (ParseXmlResponse(strResults));
            outputAttr.put (tmpAttr.getId(), tmpAttr);
          }
        } catch (MalformedURLException me) {
           log.error ("Bad URL: " + urlQuery);
//           throw new ResolutionException("Web Service unable to be queried.");
        } catch (IOException ie) {
           log.error ("I/O Error while reading from URL: " + urlQuery);
//           throw new ResolutionException("Web Service unable to be queried successfully.");
        } catch (Exception e) {
           log.error ("Unknown exception while querying webservice: " + e);
//           throw new ResolutionException("Unknown error when attempting to query web service.");
        } 

        return outputAttr;
    }

    private List<IdPAttributeValue<String>> ParseXmlResponse (String xmlResults) {

       List<IdPAttributeValue<String>> attrVals = null;

       log.debug("Parsing XML Response: " + xmlResults);

       try {
          StringReader           xmlReader = new StringReader(xmlResults);
          DocumentBuilderFactory dbf       = DocumentBuilderFactory.newInstance();
          DocumentBuilder        db        = dbf.newDocumentBuilder();
          Document               doc       = db.parse(new InputSource(xmlReader));


          Element root = doc.getDocumentElement();
          NodeList nl  = root.getChildNodes ();
          if (nl != null && nl.getLength() > 0) {
             log.debug("Found  Root Element (" + nl + ") with child count: " + nl.getLength());
             attrVals = Lists.newArrayListWithExpectedSize(nl.getLength());
             for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element)nl.item(i);
                attrVals.add(new StringAttributeValue(el.getNodeName()));
                log.debug("   - Adding attribute value: " + el.getNodeName());
             }
          }
          else
          {
            log.debug("Node List Empty for: " + root);
          }

       } catch (Exception e) {
          log.error("Attribute Resolution Failure Parsing DPS Query Response: " + xmlResults);
       }

       return attrVals;
    }

    private static String now() {
            Date date = new Date();
            String pattern = "EEE, dd MMM yyyy HH:mm:ss zzz";
            TimeZone GMT = TimeZone.getTimeZone("GMT");

            SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
            formatter.setTimeZone(GMT);
            return formatter.format(date);
      }


    private static String sign(URL url, String verb, URLConnection connection, String secret) throws Exception {
            String date = connection.getRequestProperty("Date");
            if (date == null) {
                  date = now();
                  connection.addRequestProperty("Date", date);
            }
            String md5 = connection.getRequestProperty("CONTENT-MD5");
            if (md5 == null) {
                  md5 = "";
            }
            String contentType = connection.getRequestProperty("ContentType");
            if (contentType == null) {
                  contentType = "";
            }
            String resource = url.getPath();
            if (url.getQuery() != null) {
                  resource += "?" + url.getQuery();
            }
            String stringToSign = String.format("%s%s\n%s\n%s\n%s\n%s", secret,
                        verb, md5, contentType, date, resource);
            //System.out.println(stringToSign);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(stringToSign.getBytes());

            char[] hexadecimals = new char[hash.length * 2];
            for (int i = 0; i < hash.length; ++i) {
                  for (int j = 0; j < 2; ++j) {
                        int value = (hash[i] >> (4 - 4 * j)) & 0xf;
                        char base = (value < 10) ? ('0') : ('a' - 10);
                        hexadecimals[i * 2 + j] = (char) (base + value);
               }
        }
        return new String(hexadecimals);
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
