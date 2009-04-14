// This code is part of camel-soap component. 
// Thanks to Tom Fennelly from Jboss Group
// 
// Copyright (C) 2009  Wilson Freitas - http://wilsondev.blogspot.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

package com.wfreitas.camelsoap.util;

import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import org.apache.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Utilities for SOAP messages processing
 * 
 * Thanks to Tom Fennelly from Jboss Group
 * 
 * @author wilson
 *
 */
public class OGNLUtils {
	private static Logger logger = Logger.getLogger(OGNLUtils.class);
    public static final String JBOSSESB_SOAP_NS = "http://jbossesb.jboss.org/soap";
    public static final String JBOSSESB_SOAP_NS_PREFIX = "jbossesb-soap:";
    public static final String OGNL_ATTRIB = "ognl";
    public static final String IS_COLLECTION_ATTRIB = "is-collection";

    private enum SOAPNameSpaces {
		SOAP_1_1("http://schemas.xmlsoap.org/soap/envelope/"), SOAP_1_2(
				"http://www.w3.org/2003/05/soap-envelope");

		private String nameSpace;

		SOAPNameSpaces(final String nameSpace) {
			this.nameSpace = nameSpace;
		}

		public String getNameSpace() {
			return nameSpace;
		}
	}

    public static Object getParameter(String ognl, Map params) {
        Object param;

        // Try getting the parameter from the params Map using the
        // raw OGNL expression as the key...
        param = params.get(ognl);
        if(param == null) {
            // And if that didn't work, try using the OGNL expression to extract the param
            // from an Object Graph using the OGNL toolkit...
            try {
                param = Ognl.getValue(ognl, params);
            } catch (OgnlException e) {
                if(logger.isDebugEnabled()) {
                    logger.debug("OGNL Error. Could not retrieve parameter value for " + ognl);
                }
            }
        }

        return (param != null?param:"");
    }

    public static String getOGNLExpression(Element element) {
        StringBuffer ognlExpression = new StringBuffer();
        Node parent = element.getParentNode();
        boolean isInBody = false;

        ognlExpression.append(getOGNLToken(element));

        while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
            Element parentElement = (Element) parent;
            String parentName = DOMUtil.getName(parentElement);

            if (parentName.equalsIgnoreCase("body") &&
                    parent.getNamespaceURI().equalsIgnoreCase("http://schemas.xmlsoap.org/soap/envelope/")) {
                isInBody = true;
                break;
            }

            String preassignedOgnl = parentElement.getAttributeNS(JBOSSESB_SOAP_NS, OGNL_ATTRIB);
            if(preassignedOgnl != null && !preassignedOgnl.equals("")) {
                ognlExpression.insert(0, "." + preassignedOgnl);
                isInBody = true;
                break;
            } else {
                ognlExpression.insert(0, getOGNLToken(parentElement));
            }
            parent = parent.getParentNode();
        }

        if(!isInBody) {
            return "";
        }

        // Remove the leading '.'
        ognlExpression.deleteCharAt(0);

        return ognlExpression.toString();
    }

    
    public static String getOGNLExpression(Element element, String nameSpace) {
		StringBuffer ognlExpression = new StringBuffer();
		Node parent = element.getParentNode();
		boolean isInBody = false;

		ognlExpression.append(getOGNLToken(element));

		while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
			Element parentElement = (Element) parent;
			String parentName = DOMUtil.getName(parentElement);

			if (parentName.equalsIgnoreCase("body")
					&& checkParentNameSpace(parent.getNamespaceURI(), nameSpace)) {
				isInBody = true;
				break;
			}

			String preassignedOgnl = parentElement.getAttributeNS(
					JBOSSESB_SOAP_NS, OGNL_ATTRIB);
			if (preassignedOgnl != null && !preassignedOgnl.equals("")) {
				ognlExpression.insert(0, "." + preassignedOgnl);
				isInBody = true;
				break;
			} else {
				ognlExpression.insert(0, getOGNLToken(parentElement));
			}
			parent = parent.getParentNode();
		}

		if (!isInBody) {
			return "";
		}

		// Remove the leading '.'
		ognlExpression.deleteCharAt(0);

		return ognlExpression.toString();
	}

	/**
	 * Will try to match the argument parentNS to a enum of default SOAP
	 * namespaces. If no match is made for a default SOAP namespace, this method
	 * will try to match the argument namespace.
	 * 
	 * @param parentNS
	 * @param namespace
	 * @return <code>true</code> if a namespace match has been made.
	 */
	protected static boolean checkParentNameSpace(final String parentNS,
			String namespace) {
		if (parentNS == null)
			return false;

		SOAPNameSpaces[] defaultNamespaces = SOAPNameSpaces.values();
		for (SOAPNameSpaces defaultNS : defaultNamespaces) {
			if (parentNS.equalsIgnoreCase(defaultNS.getNameSpace()))
				return true;
		}

		return parentNS.equalsIgnoreCase(namespace);
	}

    
    public static String getOGNLToken(Element element) {
        String localName = element.getLocalName();
        String ognlToken;

        if (assertIsParentCollection(element)) {
            int count = DOMUtil.countElementsBefore(element, element.getTagName());
            ognlToken = "[" + count + "]";
        } else {
            ognlToken = "." + localName;
        }

        return ognlToken;
    }

    private static boolean assertIsCollection(Element element) {
        if(element.getAttributeNS(JBOSSESB_SOAP_NS, IS_COLLECTION_ATTRIB).equals("true")) {
            // It's already been attributed... no need to check for the soapui comment...
            return true;
        }

        Comment firstComment = (Comment) DOMUtil.getFirstChildByType(element, Node.COMMENT_NODE);

        // TODO: Get Ole (soapUI) to add an attribute to the collection element - better than looking for this comment.
        if(firstComment != null && firstComment.getNodeValue().indexOf("1 or more repetitions") != -1) {
            return true;
        }

        return false;
    }

    private static boolean assertIsParentCollection(Element element) {
        Node parent = element.getParentNode();
        if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE && assertIsCollection((Element)parent)) {
            return true;
        }
        return false;
    }

}
