
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

/** Tests for {@link TestDataConnector}
 *  *
 *   */
public class TestDataConnectorTest {


    public static ResolverPluginDependency makeResolverPluginDependency(String pluginId, String attributeId) {
        ResolverPluginDependency retVal = new ResolverPluginDependency(pluginId);
        retVal.setDependencyAttributeId(attributeId);
        return retVal;
    }

   @Test public void testInit() {

       // Simple Test Framework Verification
        TestDataConnector connector = new TestDataConnector();
        connector.setId("Test");

        Assert.assertNull(connector.getUidAttribute());
        Assert.assertNull(connector.getPathToAttributeFiles());
        
        connector.setUidAttribute("uid");
        connector.setPathToAttributeFiles("/tmp");

        try {
          connector.initialize();
        } catch ( ComponentInitializationException e ) {
           Assert.fail ("Component Initialization Exception");
        }
        
   }


}

