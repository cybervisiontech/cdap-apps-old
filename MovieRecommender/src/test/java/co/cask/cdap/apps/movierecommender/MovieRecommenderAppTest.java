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

package co.cask.cdap.apps.movierecommender;

import co.cask.cdap.common.http.HttpRequest;
import co.cask.cdap.common.http.HttpRequests;
import co.cask.cdap.common.http.HttpResponse;
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
import com.google.gson.reflect.TypeToken;
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
 * Test for {@link MovieRecommenderApp}
 */
public class MovieRecommenderAppTest extends TestBase {

  private static final Gson GSON = new Gson();
  private static final Logger LOG = LoggerFactory.getLogger(MovieRecommenderAppTest.class);
  public static final int SERVICE_STATUS_CHECK_LIMIT = 5;


  @Test
  public void testRecommendation() throws Exception {
    // Deploy an Application
    ApplicationManager appManager = deployApplication(MovieRecommenderApp.class);

    // Send movies data through service
    sendMovieData(appManager);

    // Start and send ratings data to Flow
    FlowManager flowManager = appManager.startFlow("RatingsFlow");
    try {
      // Inject ratings data
      sendRatingsData(appManager);
      // Wait for the last Flowlet processing 3 events, or at most 5 seconds
      RuntimeMetrics metrics = RuntimeStats.getFlowletMetrics("MovieRecommender", "RatingsFlow", "writer");
      metrics.waitForProcessed(3, 5, TimeUnit.SECONDS);
      // Start the Spark Program
      SparkManager sparkManager = appManager.startSpark(RecommendationBuilder.class.getSimpleName());
      sparkManager.waitForFinish(10, TimeUnit.MINUTES);
    } catch (Exception e) {
      LOG.warn("Failed to send ratings data to {}", RatingsFlow.class.getSimpleName(), e);
      throw Throwables.propagate(e);
    } finally {
      flowManager.stop();
    }

    // Verify that recommendation are generated
    verifyRecommendMovieProcedure(appManager);
  }

  /**
   * Verify that the recommendations were generated using {@link RecommendMovieProcedure}
   */
  private void verifyRecommendMovieProcedure(ApplicationManager appManager) throws IOException, ParseException {
    ProcedureManager procedureManager = appManager.startProcedure(RecommendMovieProcedure.class.getSimpleName());
    try {
      ProcedureClient client = procedureManager.getClient();
      Map<String, String> params = new HashMap<String, String>();
      params.put("userId", "1");
      String response = client.query("getRecommendation", params);

      Map<String, String[]> responseMap = GSON.fromJson(response, new TypeToken<Map<String, String[]>>() {
      }.getType());
      Assert.assertTrue(responseMap.containsKey(RecommendMovieProcedure.RATED_KEY) &&
                          responseMap.get(RecommendMovieProcedure.RATED_KEY).length > 0);
      Assert.assertTrue(responseMap.containsKey(RecommendMovieProcedure.RECOMMENDED_KEY) &&
                          responseMap.get(RecommendMovieProcedure.RECOMMENDED_KEY).length > 0);
    } catch (Exception e) {
      LOG.warn("Failed to call procedure {} to get recommendations", RecommendMovieProcedure.class.getSimpleName(), e);
    } finally {
      procedureManager.stop();
    }
  }

  /**
   * Sends movies data to {@link MovieDictionaryService}
   */
  private void sendMovieData(ApplicationManager applicationManager) {
    String moviesData = "0::Movie0\n1::Movie1\n2::Movie2\n3::Movie3\n";
    ServiceManager serviceManager = applicationManager.startService(MovieDictionaryService.class.getSimpleName());
    try {
      serviceStatusCheck(serviceManager);
    } catch (InterruptedException e) {
      LOG.error("Failed to start {} service", MovieDictionaryService.class.getSimpleName(), e);
      throw Throwables.propagate(e);
    }
    try {
      URL url = new URL(serviceManager.getServiceURL(), "storemovies");
      HttpRequest request = HttpRequest.post(url).withBody(moviesData, Charsets.UTF_8).build();
      HttpResponse response = HttpRequests.execute(request);
      Assert.assertEquals(200, response.getResponseCode());
      LOG.debug("Sent movies data");
    } catch (IOException e) {
      LOG.warn("Failed to send movies data to {}", MovieDictionaryService.class.getSimpleName(), e);
      throw Throwables.propagate(e);
    } finally {
      serviceManager.stop();
    }
  }

  /**
   * Send some ratings to {@link RatingsFlow}
   */
  private void sendRatingsData(ApplicationManager appManager) throws IOException {
    StreamWriter streamWriter = appManager.getStreamWriter("ratingsStream");
    streamWriter.send("0::0::3");
    streamWriter.send("0::1::4");
    streamWriter.send("1::1::4");
    streamWriter.send("1::2::4");
    LOG.debug("Send ratings data");
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
