package com.ukonnra.wonderland.springelectrontest.hateoas;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class HateoasQueryParamResolver implements HandlerMethodArgumentResolver {
  private static final Pattern FILTER_REGEX = Pattern.compile("filter\\[(.+)]");
  private static final String SORT_QUERY = "sort";
  private static final String INCLUDE_QUERY = "include";
  private static final String SPLITERATOR_COMMA = ",";
  private final ObjectMapper objectMapper;

  public HateoasQueryParamResolver(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(ParameterObject.class);
  }

  private @Nullable List<String> parseParamValue(final String[] value) {
    final var parsed =
        Stream.of(value)
            .flatMap(s -> s == null ? Stream.of("") : Arrays.stream(s.split(SPLITERATOR_COMMA)))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .distinct()
            .toList();
    return parsed.isEmpty() ? null : parsed;
  }

  private boolean matchAndPut(
      final Map<String, Object> map,
      final Pattern pattern,
      final String key,
      final List<String> parsedValue) {
    final var filterMatch = pattern.matcher(key);
    if (filterMatch.find()) {
      map.put(filterMatch.group(1), parsedValue.size() == 1 ? parsedValue.get(0) : parsedValue);
      return true;
    }
    return false;
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    final var filter = new HashMap<String, Object>();
    final var sort = new ArrayList<String>();
    final var include = new HashSet<String>();

    for (final var e : webRequest.getParameterMap().entrySet()) {
      final var parsedParamValue = this.parseParamValue(e.getValue());

      if (parsedParamValue == null
          || matchAndPut(filter, FILTER_REGEX, e.getKey(), parsedParamValue)) {
        continue;
      }

      if (e.getKey().equals(SORT_QUERY)) {
        sort.addAll(parsedParamValue);
      } else if (e.getKey().equals(INCLUDE_QUERY)) {
        include.addAll(parsedParamValue);
      }
    }
    return this.objectMapper.convertValue(
        Map.of("filter", filter, SORT_QUERY, sort, INCLUDE_QUERY, include),
        parameter.getParameterType());
  }
}
