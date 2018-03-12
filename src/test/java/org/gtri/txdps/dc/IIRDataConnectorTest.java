
package org.gtri.txdps.dc; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AbstractDataConnector;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.authn.AuthenticationResult;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.idp.saml.authn.principal.AuthenticationMethodPrincipal;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;

import org.opensaml.profile.context.ProfileRequestContext;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opensaml.security.httpclient.HttpClientSecurityParameters;
import org.opensaml.security.httpclient.impl.SecurityEnhancedHttpClientSupport;
import org.apache.http.client.HttpClient;



/** Tests for {@link TestDataConnector}
 *  *
 *   */
public class IIRDataConnectorTest {


   @Test public void testInit() {

       // Basic Testing
       HttpClient myclient = HttpClientBuilder.create()
                    .setSSLSocketFactory(SecurityEnhancedHttpClientSupport.buildTLSSocketFactory(false, false))
                    .build();

       Assert.assertNotNull (myclient); 
   }


}

