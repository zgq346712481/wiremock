/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class PortNumberTest {

	@Test
	public void canRunOnAnotherPortThan8080() {
		WireMockServer wireMockServer = new WireMockServer(8090);
		wireMockServer.start();
		WireMockTestClient wireMockClient = new WireMockTestClient(8090);
		
		wireMockClient.addResponse(MappingJsonSamples.BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER);
		WireMockResponse response = wireMockClient.get("/a/registered/resource");
		assertThat(response.statusCode(), is(401));
		
	}
}
