package config.util;

import java.util.Set;
import java.util.Map;

public interface ArchiveExtractor {
  public Set<Map.Entry<String, String>> getEntries();
}
