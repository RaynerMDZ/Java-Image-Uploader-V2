package com.multipartfile.services;

import com.multipartfile.entity.Picture;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * @author Rayner MDZ
 */
public interface PictureService {

  String getType();
  Iterable<Picture> getAllPictures();
  Optional<Picture> getPictureById(Integer id);
  Optional<Picture> saveOrUpdatePicture(Picture picture, MultipartFile file);
  boolean deletePictureById(Integer id);
}
