package org.gtri.txdps.config;

import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;


/**
 * Spring namespace handler for the GFIPM BAE data connector namespace.
 */
public class TXDPSNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for this handler. */
    public static final String NAMESPACE = "urn:global:txdps:1.1:resolver";
    
    /** {@inheritDoc} */
    public void init() {
        // Register GFIPM Data Connector Parsers.
        registerBeanDefinitionParser(TestDataConnectorParser.TYPE_NAME,  new TestDataConnectorParser());
        registerBeanDefinitionParser(TxDPSDataConnectorParser.TYPE_NAME, new TxDPSDataConnectorParser());
        registerBeanDefinitionParser(IIRDataConnectorParser.TYPE_NAME, new IIRDataConnectorParser());
    }

}
