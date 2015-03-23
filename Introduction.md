## What is camel-soap? ##

_camel-soap_ is an Apache Camel component that provides support for zero-code SOAP requests from Camel Routes.

Camel distro already provides the excellent camel-cxf component. CXF component is very nice and powerful but the implementation of a SEI is mandatory. In some cases it is more convenient to call web services based only on the WSDL.

Right now camel-soap is a producing only component which means it is not possible to create endpoints inside the from() method in camel routes. Support for exchange producing is on camel-soap road map.

## Using camel-soap ##

camel-soap is a maven based project but it is not yet available in any maven public repository. To include camel-soap in your project you need to download the source code and build it running the following command from camel-soap source root dir:

$ mvn install

If the previous command run with no errors camel-soap component will be installed in your maven local repository and can be added to your project pom.xml as a dependency:
```
<dependency>
  <groupId>com.wfreitas</groupId>
  <artifactId>camel-soap</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```

After adding camel-soap to your project you can use it in camel routes to invoke web services:

```

  String WSDL = "http://www.webservicex.net/sunsetriseservice.asmx%3FWSDL";
  String ADDRESS = "http://www.webservicex.net/sunsetriseservice.asmx";

  from("timer:testTimer?period=1000").process(
    new Processor(){
      public void process(Exchange exchange) throws Exception {

        // camel-soap converts a map to actual SOAP method parameters
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
    // All you need to provide is:
    //   1. WSDL location
    //   2. Web service address
    //   3. Operation to invoke
    .to("soap:testWebservice?wsdlUrl=" + WSDL + "&serviceAddress=" + ADDRESS + "&operationName=GetSunSetRiseTime")
      .process(new Processor(){
          public void process(Exchange exchange) throws Exception {
            // camel-soap converts the web service response to a map
            Map body = (Map)exchange.getIn().getBody();
            System.out.println("Response from web service as a Map: " + body);
        }
      });

```

camel-soap consumes _java.util.Map_ objects and convert them to SOAP messages. The web service result is also converted to _java.util.Map_ and can be consumed by the next endpoint in the route.

## Endpoint parameters ##
| **wsdlUrl** | Target web service WSDL location |
|:------------|:---------------------------------|
| **serviceAddress** | Web service address |
| **operationName** | Method to be invoked | |