package co.cask.cdap.netlens.app.anomaly;

import co.cask.cdap.netlens.app.anomaly.AnomalyFanOutFlowlet;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class AnomalyFanOutFlowletTest {
  private static final Gson GSON = new Gson();

  @Test
  public void testSubsetOfSingle() {
    Map<String, String> map = Maps.newTreeMap();
    map.put("a", "1");

    List<Map<String,String>> allSubsets = AnomalyFanOutFlowlet.getAllSubsets(map, map.size());
    Assert.assertEquals(2, allSubsets.size());

    Map<String, String> subset = allSubsets.get(0);
    Map<String, String> empty;
    Map<String, String> notEmpty;
    if (subset.isEmpty()) {
      empty = subset;
      notEmpty = allSubsets.get(1);
    } else {
      notEmpty = subset;
      empty = allSubsets.get(1);
    }

    Assert.assertTrue(empty.isEmpty());
    Assert.assertEquals(1, notEmpty.size());
    Assert.assertEquals("1", notEmpty.get("a"));
  }

  @Test
  public void testSubsetOfMultiple() {
    Map<String, String> map = Maps.newTreeMap();
    map.put("a", "1");
    map.put("b", "2");

    List<Map<String,String>> allSubsets = AnomalyFanOutFlowlet.getAllSubsets(map, map.size());
    Assert.assertEquals(4, allSubsets.size());

    List<String> maps = mapsAsStrings(allSubsets);
    Assert.assertTrue(maps.contains(mapAsString()));
    Assert.assertTrue(maps.contains(mapAsString("a", "1")));
    Assert.assertTrue(maps.contains(mapAsString("b", "2")));
    Assert.assertTrue(maps.contains(mapAsString("a", "1", "b", "2")));
  }

  @Test
  public void testMaxSubsetSize() {
    Map<String, String> map = Maps.newTreeMap();
    map.put("a", "1");
    map.put("b", "2");
    map.put("c", "3");

    List<Map<String,String>> allSubsets = AnomalyFanOutFlowlet.getAllSubsets(map, 2);
    Assert.assertEquals(7, allSubsets.size());

    List<String> maps = mapsAsStrings(allSubsets);
    Assert.assertTrue(maps.contains(mapAsString()));
    Assert.assertTrue(maps.contains(mapAsString("a", "1")));
    Assert.assertTrue(maps.contains(mapAsString("b", "2")));
    Assert.assertTrue(maps.contains(mapAsString("c", "3")));
    Assert.assertTrue(maps.contains(mapAsString("a", "1", "b", "2")));
    Assert.assertTrue(maps.contains(mapAsString("b", "2", "c", "3")));
    Assert.assertTrue(maps.contains(mapAsString("a", "1", "c", "3")));
  }

  private List<String> mapsAsStrings(List<Map<String, String>> allSubsets) {
    return Lists.transform(allSubsets, new Function<Map<String, String>, String>() {
      @Nullable
      @Override
      public String apply(@Nullable Map<String, String> stringStringMap) {
        return mapAsString(stringStringMap);
      }
    });
  }

  // note: param should be sorted
  private String mapAsString(String... keyVals) {
    assert keyVals.length % 2 == 0;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keyVals.length; i += 2) {
      sb.append(keyVals[i]).append(keyVals[i + 1]);
    }
    return sb.toString();
  }

  private String mapAsString(Map<String, String> map) {
    Map<String, String> sorted = new TreeMap<String, String>(map);
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : sorted.entrySet()) {
      sb.append(entry.getKey()).append(entry.getValue());
    }
    return sb.toString();
  }

  private boolean equals(Map<String, String> first, Map<String, String> second) {
    // JSON representation of sorted maps should be same
    return GSON.toJson(new TreeMap<String, String>(first)).equals(GSON.toJson(new TreeMap<String, String>(second)));
  }
}
