/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.util.toolbox;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * Unit test for the {@link XsltAggregationStrategy}.
 * <p>
 * Need to use Saxon to get a predictable result: we cannot rely on the JDK's XSLT processor as it can vary across
 * platforms and JDK versions. Also, Xalan does not handle node-set properties well.
 */
public class XsltAggregationStrategyTest extends ContextTestSupport {

    @Test
    public void testXsltAggregationDefaultProperty() throws Exception {
        context.startRoute("route1");
        MockEndpoint mock = getMockEndpoint("mock:transformed");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("<?xml version=\"1.0\" encoding=\"UTF-8\"?><item>ABC</item>");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testXsltAggregationUserProperty() throws Exception {
        context.startRoute("route2");
        MockEndpoint mock = getMockEndpoint("mock:transformed");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("<?xml version=\"1.0\" encoding=\"UTF-8\"?><item>ABC</item>");
        assertMockEndpointsSatisfied();
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:src/test/resources/org/apache/camel/util/toolbox?noop=true&antInclude=*.xml")
                        .routeId("route1").noAutoStartup()
                        .aggregate(new XsltAggregationStrategy("org/apache/camel/util/toolbox/aggregate.xsl")
                                .withSaxon())
                        .constant(true)
                        .completionFromBatchConsumer()
                    .log("after aggregate body: ${body}")
                    .to("mock:transformed");

                from("file:src/test/resources/org/apache/camel/util/toolbox?noop=true&antInclude=*.xml")
                        .routeId("route2").noAutoStartup()
                        .aggregate(new XsltAggregationStrategy("org/apache/camel/util/toolbox/aggregate-user-property.xsl")
                                .withSaxon().withPropertyName("user-property"))
                        .constant(true)
                        .completionFromBatchConsumer()
                        .log("after aggregate body: ${body}")
                        .to("mock:transformed");
            }
        };
    }
}