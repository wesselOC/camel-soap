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

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultComponent;

public class SoapComponent extends DefaultComponent<Exchange> {

	@Override
	protected Endpoint<Exchange> createEndpoint(String uri, String remaining, Map parameters) throws Exception {
		
		SoapEndpoint endpoint = new SoapEndpoint(uri, this);
		
		setProperties(endpoint, parameters);
		
		return endpoint;
	}

}
