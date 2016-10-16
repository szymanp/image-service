package com.metapx.server.data_model.resource.infrastructure;

import org.jooq.DSLContext;

public interface RequestParameters {
  DSLContext getDslContext();
  UrlResolver getUrlResolver();
}
