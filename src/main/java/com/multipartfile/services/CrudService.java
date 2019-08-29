package com.multipartfile.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * @author Rayner MDZ
 */
public interface CrudService<T, I> {

  Iterable<T> getAllPictures();
  Optional<T> getPictureById(I id);
  Optional<T> saveOrUpdatePicture(T obj, MultipartFile file);
  boolean deletePictureById(I id);
}
