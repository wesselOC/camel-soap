// This code is part of camel-soap component. 
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
package com.wfreitas.camelsoap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

public class SoapProducer extends DefaultProducer<Exchange> {

	private SoapEndpoint endpoint;

	private SoapClient soapClient;
	
	public SoapProducer(Endpoint<Exchange> endpoint) {
		super(endpoint);
		this.endpoint = (SoapEndpoint)endpoint;
		soapClient = new SoapClient();
	}

	public void process(Exchange exchange) throws Exception {
		Object soapMessage = exchange.getIn().getBody();
		
		if(soapMessage instanceof Map){
			Map params = (Map)soapMessage;
			
			String operation = endpoint.getOperationName();
			Map newParams = new HashMap();
			
			for(Iterator it = params.keySet().iterator(); it.hasNext();){
				String param = (String)it.next();
				newParams.put(operation + "." + param , params.get(param));
				
			}
			Map response = soapClient.sendRequest(endpoint.getServiceAddress(), operation, newParams, endpoint.getWsdlUrl());
			
			exchange.getOut().setBody(response);
			
		}else{
			throw new RuntimeException(String.format("Unsupported message format. Was [%s] expected [%s]", soapMessage.getClass().getName(), Map.class.getName()));
		}
	}

}
