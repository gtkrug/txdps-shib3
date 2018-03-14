# txdps-shib3
IDP3 Dataconnector for Texas DPS

This includes 3 data connectors.  One test connector that reads attribute files found on the filesystem, one that queries an internal TXDPS webservice, and one that queries IIR's 28CFR Certification Checking Service.

Sample configurations are available here:

    <resolver:DataConnector id="GfipmTest" xsi:type="txdps:Test" 
                            xmlns="urn:global:txdps:1.1:resolver"
                            pathToAttributeFiles="/opt/idp/users/"
                            uidAttribute="LocalId">
        <resolver:Dependency ref="LocalId" />
    </resolver:DataConnector>

    <resolver:DataConnector id="IIRTest" xsi:type="txdps:IIRQuery" 
                            xmlns="urn:global:txdps:1.1:resolver"
                            emailAttribute="Email"
                            attrName="CFRCertification"
                            queryUrl="https://tca.iir.com/api/LookupTestNotCompleted?code=[ISSUED BY IIR]">
        <resolver:Dependency ref="Email" />
    </resolver:DataConnector>

The above IIRQuery sample queries the IIR test service that always returns incomplete testing.  For production the queryUrl would be https://tca.iir.com/api/Lookup?code=


