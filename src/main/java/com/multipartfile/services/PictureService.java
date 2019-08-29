package com.multipartfile.services;

import com.multipartfile.entity.Picture;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Rayner MDZ
 */
public interface PictureService extends CrudService<Picture, Integer> {

  String getType();
}
