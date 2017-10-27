/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package migration.client;

import migration.client._110Specific.ResourceModifier;
import migration.client._110Specific.dto.SynapseDTO;
import migration.client._200Specific.ResourceModifier200;
import migration.util.Constants;
import migration.util.ResourceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * This class contains all the methods which is used to migrate APIs from APIManager 1.8.0 to APIManager 1.9.0.
 * The migration performs in database, registry and file system
 */

public class MigrateFrom17to210 {

    private static final Log log = LogFactory.getLog(MigrateFrom17to210.class);


    public static void main(String[] args) {
        String apiFilePath = "/Users/ramindu/Downloads/synapse-configs/default/api";
        synapseAPIMigration170_190(apiFilePath);
        synapseAPIMigration190_110(apiFilePath);
        synapseAPIMigration110_200(apiFilePath);
    }

    /**
     * This method is used to migrate synapse files
     * This changes the synapse api and add the new handlers
     */
    private static void synapseAPIMigration170_190(String apiFilePath) {

        File APIFiles = new File(apiFilePath);
        File[] synapseFiles = APIFiles.listFiles();

        if (synapseFiles == null) {
            log.error("No api folder " + apiFilePath + " exists.");
        } else {
            for (File synapseFile : synapseFiles) {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setNamespaceAware(true);

                try {
                    docFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(synapseFile);

                    doc.getDocumentElement().normalize();
                    Element rootElement = doc.getDocumentElement();

                    if (Constants.SYNAPSE_API_ROOT_ELEMENT.equals(rootElement.getNodeName()) && rootElement
                            .hasAttribute(Constants.SYNAPSE_API_ATTRIBUTE_VERSION)) {
                        ResourceUtil.updateSynapseAPI(doc, synapseFile);
                    }
                } catch (ParserConfigurationException e) {
                    log.error("Parsing exception encountered for " + synapseFile.getAbsolutePath(), e);
                } catch (SAXException e) {
                    log.error("SAX exception encountered for " + synapseFile.getAbsolutePath(), e);
                } catch (IOException e) {
                    log.error("IO exception encountered for " + synapseFile.getAbsolutePath(), e);
                } catch (Exception e) {
                    log.error("Error occurred while migrating the Synapse file : " + synapseFile.getAbsolutePath(), e);
                }
            }
            log.info("End synapseAPIMigration for 190");
        }
    }

    private static void synapseAPIMigration190_110(String apiFilePath) {
        try {
            List<SynapseDTO> synapseDTOs = ResourceUtil.getVersionedAPIs(apiFilePath);
            ResourceModifier.updateSynapseConfigs(synapseDTOs);

            for (SynapseDTO synapseDTO : synapseDTOs) {
                ResourceUtil.transformXMLDocument(synapseDTO.getDocument(), synapseDTO.getFile());
            }
        } catch (Exception e) {
            log.error("Unable to do the Synapse API migration.");
        }
    }

    private static void synapseAPIMigration110_200(String apiPath) {
        List<SynapseDTO> synapseDTOs = ResourceUtil.getVersionedAPIs(apiPath);
        ResourceModifier200.updateSynapseConfigs(synapseDTOs);

        for (SynapseDTO synapseDTO : synapseDTOs) {
            ResourceModifier200.transformXMLDocument(synapseDTO.getDocument(), synapseDTO.getFile());
        }
    }
}
