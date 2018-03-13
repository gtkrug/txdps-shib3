
package org.gtri.txdps.dc; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import org.apache.http.client.ClientProtocolException;

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

import org.testng.Assert;
import org.testng.annotations.Test;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.opensaml.security.httpclient.impl.SecurityEnhancedHttpClientSupport;


/** Tests for {@link TestDataConnector}
 *  *
 *   */
public class IIRDataConnectorTest {

   String url  = "https://tca.iir.com/api/LookupTestCompleted?code=D7utRK84NO8sDTYRjh0UGP3fNXLjrH96FMlKs21YqcBpyTeZp6k/rw==";
   String json = "{ 'email': 'user@domain.com' }"; 


   @Test public void testInit() {

       // Basic Testing
       CloseableHttpClient myclient = HttpClientBuilder.create().setSSLSocketFactory(SecurityEnhancedHttpClientSupport.buildTLSSocketFactory(false, false)).build();

       Assert.assertNotNull (myclient); 

       HttpPost myPost = new HttpPost(url);
       EntityBuilder builder = EntityBuilder.create();
       
       myPost.setEntity (builder.setText(json).build());
       myPost.setHeader ("Accept", "application/json");
       myPost.setHeader ("Content-type", "application/json");

       try {
         CloseableHttpResponse myResp = myclient.execute(myPost);
         Assert.assertEquals(myResp.getStatusLine().getStatusCode(), 200);
       } catch (ClientProtocolException e) {
         Assert.fail ("Protocol Exception when comunicating with server: ", e);
       } catch (IOException e) {
         Assert.fail ("I/O Exception when comunicating with server: ", e);
       }

       
   }

}

