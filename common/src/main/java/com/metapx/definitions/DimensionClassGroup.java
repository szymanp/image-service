package com.metapx.definitions;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public interface DimensionClassGroup {
  public DimensionClass get(String name);
  public Optional<DimensionClass> getOpt(String name);
  public Stream<DimensionClass> stream();
  
  public class Provider implements DimensionClassGroup {
    private final HashMap<String, DimensionClass> dims = new HashMap<String, DimensionClass>();
    
    public DimensionClassGroup add(DimensionClass dim) {
      dims.put(dim.getName().toLowerCase(), dim);
      return this;
    }
    
    @Override
    public DimensionClass get(String name) {
      return dims.get(name.toLowerCase());
    }
    
    @Override
    public Optional<DimensionClass> getOpt(String name) {
      final String key = name.toLowerCase();
      return dims.containsKey(key) ? Optional.of(dims.get(key)) : Optional.empty();
    }
    
    @Override
    public Stream<DimensionClass> stream() {
      return dims.values().stream();
    }
  }
}
