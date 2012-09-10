package org.plovr;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generates a Closure Templates Soy map literal for the plovr config options.
 * This class is a modified version of <a target="_blank"
 * href="http://code.google.com/p/plovr/source/browse/src/org/plovr/ConfigOptionDocumentationGenerator.java">
 * org.plovr.ConfigOptionDocumentationGenerator.java</a>, that generates a Soy
 * Map literal instead of HTML.
 *
 * @author bolinfest{at}gmail{dot}com (Michael Bolin)
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public final class ConfigOptionSoyDocGenerator {



  private static class OptionDescriptor {
    final String name;
    boolean acceptsString = false;
    boolean acceptsBoolean = false;
    boolean acceptsNumber = false;
    boolean acceptsArray = false;
    boolean acceptsObject = false;
    boolean supportsQueryDataOverride = false;

    OptionDescriptor(String name) {
      Preconditions.checkNotNull(name);
      this.name = name;
    }

    SoyMapData asSoyMapData() {
      List<String> acceptedValues = Lists.newLinkedList();
      if (acceptsString) acceptedValues.add("string");
      if (acceptsBoolean) acceptedValues.add("boolean");
      if (acceptsNumber) acceptedValues.add("number");
      if (acceptsArray) acceptedValues.add("array");
      if (acceptsObject) acceptedValues.add("object");

      ImmutableMap.Builder<String, Object> builder =
          ImmutableMap.<String, Object>builder()
          .put("name", name)
          .put("acceptedValues", Joiner.on(", ").join(acceptedValues))
          .put("supportsQueryDataOverride", supportsQueryDataOverride);
      return new SoyMapData(builder.build());
    }
  }

  private static List<OptionDescriptor> createDescriptors() {
    ImmutableList.Builder<OptionDescriptor> builder = ImmutableList.builder();
    for (ConfigOption option : ConfigOption.values()) {
      OptionDescriptor descriptor = new OptionDescriptor(option.getName());
      Config.Builder configBuilder = Config.builderForTesting();

      if (testArgumentSupport(option, new JsonPrimitive("foo"), configBuilder)) {
        descriptor.acceptsString = true;
      }
      if (testArgumentSupport(option, new JsonPrimitive(true), configBuilder)) {
        descriptor.acceptsBoolean = true;
      }
      if (testArgumentSupport(option, new JsonPrimitive(42), configBuilder)) {
        descriptor.acceptsNumber = true;
      }
      if (testArgumentSupport(option, new JsonArray(), configBuilder)) {
        descriptor.acceptsArray = true;
      }
      if (testArgumentSupport(option, new JsonObject(), configBuilder)) {
        descriptor.acceptsObject = true;
      }
      if (testQueryParamSupport(option, configBuilder)) {
        descriptor.supportsQueryDataOverride = true;
      }

      builder.add(descriptor);
    }
    return builder.build();
  }

  /**
   * Tests whether a {@link ConfigOption} supports a JSON type as an argument by
   * giving it a dummy value and checking whether it throws an
   * {@link UnsupportedOperationException}.
   */
  private static boolean testArgumentSupport(
      ConfigOption option, JsonElement dummyValue, Config.Builder builder) {
    try {
      option.update(builder, dummyValue);
    } catch (UnsupportedOperationException e) {
      return false;
    } catch (Throwable t) {
      // It is given a dummy value, so it cannot be expected to work.
      return true;
    }
    return true;
  }

  /**
   * Tests whether a {@link ConfigOption} can be overriden by query data.
   */
  private static boolean testQueryParamSupport(ConfigOption option,
      Config.Builder builder) {
    URI uri;
    try {
      uri = new URI(String.format("http://localhost:9810/compile?%s=test",
          option.getName()));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    try {
      QueryData data = QueryData.createFromUri(uri);
      return option.update(builder, data);
    } catch (Throwable t) {
      return true;
    }
  }

  private static String generateHtml() {
    List<String> allNames = Lists.newArrayList();
    for (ConfigOption option : ConfigOption.values()) {
      allNames.add(option.getName());
    }
    Collections.sort(allNames);

    List<OptionDescriptor> descriptors = createDescriptors();
    Function<OptionDescriptor,SoyMapData> f = new Function<OptionDescriptor,SoyMapData>() {
      @Override
      public SoyMapData apply(OptionDescriptor descriptor) {
        return descriptor.asSoyMapData();
      }
    };
    List<SoyMapData> descriptorData = Lists.transform(descriptors, f);
    Map<String, SoyMapData> configNameToDescriptor = Maps.newHashMap();

    for (SoyMapData mapData : descriptorData) {
      configNameToDescriptor.put(mapData.getSingle("name").stringValue(),
          mapData);
    }

    return configNameToDescriptor.toString();
  }

  public static void main(String[] args) {
    System.out.println(generateHtml());
  }
}
