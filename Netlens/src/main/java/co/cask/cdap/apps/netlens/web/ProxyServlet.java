/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.apps.netlens.web;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proxies POST requests to a given url (specified thru cdap.host and cdap.port system properties).
 * By default proxies to http://localhost:10000.
 *
 * Needed for resolving cross-domain javascript complexities.
 */
public class ProxyServlet extends HttpServlet {
  private static final Logger LOG = LoggerFactory.getLogger(ProxyServlet.class);

  private String cdapURL;

  @Override
  public void init() throws ServletException {
    String host = System.getProperty("cdap.host");
    host = host == null ? "localhost" : host;
    String port = System.getProperty("cdap.port");
    port = port == null ? "10000" : port;
    cdapURL = String.format("http://%s:%s", host, port);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    AsyncHttpClient client =
      new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeoutInMs(1000).build());
    try {
      String url = cdapURL + req.getPathInfo();
      byte[] bytes = ByteStreams.toByteArray(req.getInputStream());
      String responseBody;
      try {
        responseBody = client.preparePost(url).setBody(bytes).execute().get().getResponseBody();
      } catch (Exception e) {
        LOG.error("handling request failed", e);
        e.printStackTrace();
        throw Throwables.propagate(e);
      }

      PrintWriter out = resp.getWriter();
      out.write(responseBody);
      out.close();
    } finally {
      client.close();
    }
  }
}
