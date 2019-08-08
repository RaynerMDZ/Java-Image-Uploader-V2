package com.multipartfile.services.implementations;

import com.multipartfile.entity.Picture;
import com.multipartfile.repositories.PictureRepository;
import com.multipartfile.services.PictureService;
import com.multipartfile.util.AzureConnection;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * @author Rayner MDZ
 */
@Service
public class PictureServiceDatabaseImpl implements PictureService {

  @Qualifier(value = "PictureRepository")
  private PictureRepository repository;

  @Qualifier(value = "AzureConnection")
  private AzureConnection azureConnection;

  public PictureServiceDatabaseImpl(PictureRepository repository, AzureConnection azureConnection) {
    this.repository = repository;
    this.azureConnection = azureConnection;
  }

  /**
   *
   * @return
   */
  @Override
  public Iterable<Picture> getAllPictures() {
    return this.repository.findAll();
  }

  /**
   *
   * @param id
   * @return
   */
  @Override
  public Optional<Picture> getPictureById(Integer id) {
    return Optional.empty();
  }

  /**
   *
   * @param picture
   * @param file
   * @return
   */
  @Override
  public Optional<Picture> saveOrUpdatePicture(Picture picture, MultipartFile file) {
    return Optional.empty();
  }

  /**
   *
   * @param id
   * @return
   */
  @Override
  public boolean deletePictureById(Integer id) {
    try {
      this.repository.deleteById(id);
      return true;
    } catch (HibernateException e) {
      e.printStackTrace();
      return false;
    }
  }
}
