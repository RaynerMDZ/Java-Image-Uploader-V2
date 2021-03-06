package com.multipartfile.services.implementations;

import com.multipartfile.services.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Rayner MDZ
 */
@Component
public class PictureFactory {

  private static final Map<String, PictureService> pictureServiceCache = new HashMap<>();

  @Autowired
  public PictureFactory(Set<PictureService> services) {
    for (PictureService service : services) {
      pictureServiceCache.put(service.getType(), service);
    }
  }

  public PictureService getService(String type) {
    type = type.toLowerCase();
    PictureService service = pictureServiceCache.get(type);
    if (service == null) throw new RuntimeException("Unknown service type: " + type);
    return service;
  }
}
