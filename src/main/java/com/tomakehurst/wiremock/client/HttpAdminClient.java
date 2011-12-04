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
package com.tomakehurst.wiremock.client;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.tomakehurst.wiremock.http.MimeType.JSON;
import static com.tomakehurst.wiremock.mapping.JsonMappingBinder.buildVerificationResultFrom;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.tomakehurst.wiremock.global.GlobalSettings;
import com.tomakehurst.wiremock.mapping.JsonMappingBinder;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.verification.VerificationResult;

public class HttpAdminClient implements AdminClient {
	
	private static final String ADMIN_URL_PREFIX = "http://%s:%d/__admin";
	private static final String LOCAL_WIREMOCK_NEW_RESPONSE_URL = ADMIN_URL_PREFIX + "/mappings/new";
	private static final String LOCAL_WIREMOCK_RESET_URL = ADMIN_URL_PREFIX + "/reset";
	private static final String LOCAL_WIREMOCK_COUNT_REQUESTS_URL = ADMIN_URL_PREFIX + "/requests/count";
	private static final String WIREMOCK_GLOBAL_SETTINGS_URL = ADMIN_URL_PREFIX + "/settings";
	
	private String host;
	private int port;
	
	public HttpAdminClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void addResponse(String responseSpecJson) {
		int status = postJsonAndReturnStatus(newMappingUrl(), responseSpecJson);
		if (status != HTTP_CREATED) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	@Override
	public void resetMappings() {
		int status = postEmptyBodyAndReturnStatus(resetUrl());
		assertStatusOk(status);
	}

	private void assertStatusOk(int status) {
		if (status != HTTP_OK) {
			throw new RuntimeException("Returned status code was " + status);
		}
	}
	
	@Override
	public int getRequestsMatching(RequestPattern requestPattern) {
		String json = JsonMappingBinder.write(requestPattern);
		String body = postJsonAssertOkAndReturnBody(requestsCountUrl(), json, HTTP_OK);
		VerificationResult verificationResult = buildVerificationResultFrom(body);
		return verificationResult.getCount();
	}
	
	@Override
	public void updateGlobalSettings(GlobalSettings settings) {
		String json = JsonMappingBinder.write(settings);
		postJsonAssertOkAndReturnBody(globalSettingsUrl(), json, HTTP_OK);
	}

	private int postJsonAndReturnStatus(String url, String json) {
		HttpPost post = new HttpPost(url);
		try {
			if (json != null) {
				post.setEntity(new StringEntity(json, JSON.toString(), "utf-8"));
			}
			HttpResponse response = new DefaultHttpClient().execute(post);
			return response.getStatusLine().getStatusCode();
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String postJsonAssertOkAndReturnBody(String url, String json, int expectedStatus) {
		HttpPost post = new HttpPost(url);
		try {
			if (json != null) {
				post.setEntity(new StringEntity(json, JSON.toString(), "utf-8"));
			}
			HttpResponse response = new DefaultHttpClient().execute(post);
			if (response.getStatusLine().getStatusCode() != expectedStatus) {
				throw new VerificationException("Expected status " + expectedStatus);
			}
			
			String body = new String(toByteArray(response.getEntity().getContent()));
			return body;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private int postEmptyBodyAndReturnStatus(String url) {
		return postJsonAndReturnStatus(url, null);
	}

	private String newMappingUrl() {
		return String.format(LOCAL_WIREMOCK_NEW_RESPONSE_URL, host, port);
	}
	
	private String resetUrl() {
		return String.format(LOCAL_WIREMOCK_RESET_URL, host, port);
	}
	
	private String requestsCountUrl() {
		return String.format(LOCAL_WIREMOCK_COUNT_REQUESTS_URL, host, port);
	}

	private String globalSettingsUrl() {
		return String.format(WIREMOCK_GLOBAL_SETTINGS_URL, host, port);
	}
}
