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

package co.cask.cdap.examples.moviesteer;

import co.cask.cdap.common.http.HttpRequest;
import co.cask.cdap.common.http.HttpRequests;
import co.cask.cdap.common.http.HttpResponse;
import co.cask.cdap.moviesteer.app.MovieDictionaryService;
import co.cask.cdap.moviesteer.app.MovieSteerApp;
import co.cask.cdap.moviesteer.app.PredictionProcedure;
import co.cask.cdap.test.ApplicationManager;
import co.cask.cdap.test.FlowManager;
import co.cask.cdap.test.ProcedureClient;
import co.cask.cdap.test.ProcedureManager;
import co.cask.cdap.test.RuntimeMetrics;
import co.cask.cdap.test.RuntimeStats;
import co.cask.cdap.test.ServiceManager;
import co.cask.cdap.test.SparkManager;
import co.cask.cdap.test.StreamWriter;
import co.cask.cdap.test.TestBase;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Test MovieSteerApp
 */
public class MovieSteerAppTest extends TestBase {

  private static final Gson GSON = new Gson();
  private static final Logger LOG = LoggerFactory.getLogger(MovieSteerAppTest.class);
  public static final int SERVICE_STATUS_CHECK_LIMIT = 5;

  public static final String APPLICATION_NAME = "MovieSteer";
  public static final String SPARK_PROGRAM_NAME = "MovieSteerSparkProgram";
  public static final String FLOW_NAME = "RatingsFlow";
  public static final String FLOWLET_NAME = "reader";
  public static final String STREAM_NAME = "ratingsStream";
  public static final String MOVIE_UPLOAD_SERVICE_NAME = "MovieUploadService";

  @Test
  public void test() throws Exception {
    // Deploy an Application
    ApplicationManager appManager = deployApplication(MovieSteerApp.class);

    // Send movies data through service
    sendMovieData(appManager);

    // Start a Flow
    FlowManager flowManager = appManager.startFlow(FLOW_NAME);

    try {
      // Inject data
      sendData(appManager);
      // Wait for the last Flowlet processing 3 events, or at most 5 seconds
      RuntimeMetrics metrics = RuntimeStats.getFlowletMetrics(APPLICATION_NAME, FLOW_NAME, FLOWLET_NAME);
      metrics.waitForProcessed(3, 5, TimeUnit.SECONDS);
      // Start a Spark Program
      SparkManager sparkManager = appManager.startSpark(SPARK_PROGRAM_NAME);
      sparkManager.waitForFinish(360, TimeUnit.SECONDS);
    } finally {
      flowManager.stop();
    }

    // Verify data processed well
    verifyPredictionProcedure(appManager);
  }

  /**
   * verify execution of the PredictionProcedure
   */
  private void verifyPredictionProcedure(ApplicationManager appManager) throws IOException, ParseException {
    ProcedureManager procedureManager = appManager.startProcedure(
      PredictionProcedure.class.getSimpleName());
    try {
      ProcedureClient client = procedureManager.getClient();
      Map<String, String> params = new HashMap<String, String>();
      params.put("userId", "1");
      String response = client.query("getPrediction", params);
      LOG.debug("getPrediction response: " + response);
      String value = GSON.fromJson(response, String.class);
      Assert.assertNotNull(value);
    } finally {
      procedureManager.stop();
    }
  }

  /**
   * Send a few ratings to the stream
   */
  private void sendData(ApplicationManager appManager) throws IOException {
    StreamWriter streamWriter = appManager.getStreamWriter(STREAM_NAME);
    streamWriter.send("0::2::3");
    streamWriter.send("0::3::1");
    streamWriter.send("0::5::2");
    streamWriter.send("1::2::2");
    streamWriter.send("1::3::1");
    streamWriter.send("1::4::2");
  }

  /**
   * Send movie data to the upload service
   */
  private void sendMovieData(ApplicationManager applicationManager) {
    String moviesData = "1::Toy Story (1995)::Animation|Children's|Comedy" +
      "\n2::Jumanji (1995)::Adventure|Children's|Fantasy" +
      "\n3::Grumpier Old Men (1995)::Comedy|Romance";

    ServiceManager serviceManager = applicationManager.startService(MOVIE_UPLOAD_SERVICE_NAME);
    try {
      serviceStatusCheck(serviceManager);
    } catch (InterruptedException e) {
      LOG.error("Failed to start {} service", MOVIE_UPLOAD_SERVICE_NAME, e);
      throw Throwables.propagate(e);
    }
    try {
      URL url = new URL(serviceManager.getServiceURL(), "storemovies");
      HttpRequest request = HttpRequest.post(url).withBody(moviesData, Charsets.UTF_8).build();
      HttpResponse response = HttpRequests.execute(request);
      Assert.assertEquals(200, response.getResponseCode());
      LOG.debug("Sent movies data");
    } catch (IOException e) {
      LOG.warn("Failed to send movies data to {}", MOVIE_UPLOAD_SERVICE_NAME, e);
      throw Throwables.propagate(e);
    } finally {
      serviceManager.stop();
    }
  }

  /**
   * Check service to go in running state
   */
  private void serviceStatusCheck(ServiceManager serviceManger) throws InterruptedException {
    int trial = 0;
    while (trial++ < SERVICE_STATUS_CHECK_LIMIT) {
      if (serviceManger.isRunning()) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new IllegalStateException("Service didn't start in " + SERVICE_STATUS_CHECK_LIMIT + " seconds.");
  }

}
