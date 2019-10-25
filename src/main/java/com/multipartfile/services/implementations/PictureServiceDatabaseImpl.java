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
public class PictureServiceDatabaseImpl implements PictureService {

  @Qualifier(value = "PictureRepository")
  private final PictureRepository repository;

  private final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public PictureServiceDatabaseImpl(@Qualifier(value = "PictureRepository") PictureRepository repository) {
    log.info("Database service loaded!");
    this.repository = repository;
  }

  @Override
  public String getType() {
    log.info("Entering database implementation.");
    return "database";
  }

  /**
   *
   * @return
   */
  @Override
  public Iterable<Picture> getAllPictures() {
    log.info("Returning all pictures from the database.");
    return this.repository.findAll();
  }

  /**
   *
   * @param id
   * @return
   */
  @Override
  public Optional<Picture> getPictureById(Integer id) {
    log.info("Returning picture with the id: " + id + " from the database.");
    return this.repository.findById(id);
  }

  /**
   *
   * @param picture
   * @param file
   * @return Optional<Picture>
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

          log.info("Updating database picture with the id: " + picture.getId());

          encoded = Base64.encodeBase64String(file.getBytes());

          picture.setBlob(file.getBytes());
          picture.setPictureString(encoded);

          log.info("Picture updated to the database.");

          return Optional.of(repository.save(picture));

        }
      }

    } catch (IOException e) {
      log.info("An error occurred while updating a picture from the database with the id: " + picture.getId());
      e.printStackTrace();
    }

    // Creates

    try {

      log.info("Creating new picture in the database.");

      encoded = Base64.encodeBase64String(file.getBytes());

      foundPicture = new Picture();
      foundPicture.setName(picture.getName());
      foundPicture.setBlob(file.getBytes());
      foundPicture.setPictureString(encoded);
      foundPicture.setUploadMethods(picture.getUploadMethods());

      log.info("Picture created in the database.");

      return Optional.of(repository.save(foundPicture));

    } catch (IOException e) {
      log.info("An error occurred while creating the picture in the database.");
      e.printStackTrace();
    }

    return Optional.empty();
  }

  /**
   *
   * @param id
   * @return boolean
   */
  @Override
  @Transactional
  public boolean deletePictureById(Integer id) {

    log.info("Deleting picture from database.");

    try {

      this.repository.deleteById(id);

      log.info("Picture with id: " + id + " has been deleted from the database.");

      return true;
    } catch (HibernateException e) {

      log.info("An error occurred while deleting the picture with the id: " + id + " from the database.");

      e.printStackTrace();
      return false;
    }
  }
}
