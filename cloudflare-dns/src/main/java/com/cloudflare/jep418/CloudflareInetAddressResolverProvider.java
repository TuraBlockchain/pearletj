package com.cloudflare.jep418;

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;

public class CloudflareInetAddressResolverProvider extends InetAddressResolverProvider {
  @Override
  public InetAddressResolver get(Configuration configuration) {
    return new CloudflareInetAddressResolver();
  }

  @Override
  public String name() {
    return "Cloudflare Internet Address Resolver Provider";
  }
}
