package com.multipartfile.services.implementations;

import com.multipartfile.services.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rayner MDZ
 */
@Service
public class PictureFactory {

  private static final Map<String, PictureService> pictureServiceCache = new HashMap<>();

  @Autowired
  public PictureFactory(List<PictureService> services) {
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
