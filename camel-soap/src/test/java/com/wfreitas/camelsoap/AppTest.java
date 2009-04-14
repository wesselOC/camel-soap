package com.wfreitas.camelsoap;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    private static final String WSDL = "http://www.webservicex.net/sunsetriseservice.asmx%3FWSDL";
    private static final String ADDRESS = "http://www.webservicex.net/sunsetriseservice.asmx";
    
    public void testProducer() throws Exception{
    	CamelContext camelContext = new DefaultCamelContext();
    	
    	RouteBuilder builder = new RouteBuilder(){
    		
			@Override
			public void configure() throws Exception {
				from("timer:testTimer?period=1000").process(
					new Processor(){

						public void process(Exchange exchange) throws Exception {
							Map<String, String> params = new HashMap<String, String>();
							params.put("L.Latitude", "0.1");
							params.put("L.Longitude", "1");
							params.put("L.SunSetTime", "0.2");
							params.put("L.SunRiseTime", "0.2");
							params.put("L.TimeZone", "2");
							params.put("L.Day", "3");
							params.put("L.Month", "10");
							params.put("L.Year", "1974");
							
							exchange.getOut().setBody(params);
						}
						
					})
				.to("soap:testWebservice?wsdlUrl=" + WSDL + "&serviceAddress=" + ADDRESS + "&operationName=GetSunSetRiseTime")
					.process(new Processor(){

						public void process(Exchange exchange) throws Exception {
						
							Map body = (Map)exchange.getIn().getBody();
							
							System.out.println("Response from web service as a Map: " + body);
						}
						
					})
				;
			}
    		
    	};
    	
    	camelContext.addRoutes(builder);
    	camelContext.start();
    	Thread.sleep(10000);
    }
}
