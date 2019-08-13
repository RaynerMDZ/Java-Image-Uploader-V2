package com.multipartfile.services.implementations;

import com.multipartfile.entity.Picture;
import com.multipartfile.repositories.PictureRepository;
import com.multipartfile.services.PictureService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Rayner MDZ
 */
@Service
//@Profile("database")
public class PictureServiceDatabaseImpl implements PictureService {

  @Qualifier(value = "PictureRepository")
  private final PictureRepository repository;

  private final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public PictureServiceDatabaseImpl(PictureRepository repository) {
    log.info("Database service loaded!");
    this.repository = repository;
  }

  @Override
  public String getType() {
    return "database";
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
    return this.repository.findById(id);
  }

  /**
   *
   * @param picture
   * @param file
   * @return
   */
  @Override
  @Transactional
  public Optional<Picture> saveOrUpdatePicture(Picture picture, MultipartFile file) {

    Picture foundPicture;
    String encoded;

    // Updates
    try {

      if (picture.getId() != null) {
        if (getPictureById(picture.getId()).isPresent()) {

          encoded = Base64.encodeBase64String(file.getBytes());

          picture.setBlob(file.getBytes());
          picture.setPictureString(encoded);
          return Optional.of(repository.save(picture));

        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    // Creates

    try {

      encoded = Base64.encodeBase64String(file.getBytes());

      foundPicture = new Picture();
      foundPicture.setName(picture.getName());
      foundPicture.setBlob(file.getBytes());
      foundPicture.setPictureString(encoded);
      foundPicture.setUploadMethods(picture.getUploadMethods());

      return Optional.of(repository.save(foundPicture));

    } catch (IOException e) {
      e.printStackTrace();
    }

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
