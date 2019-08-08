package com.multipartfile.services.implementations;

import com.multipartfile.entity.Picture;
import com.multipartfile.repositories.PictureRepository;
import com.multipartfile.services.PictureService;
import com.multipartfile.util.Util;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Rayner MDZ
 */
@Service
@Primary
public class PictureServiceImpl implements PictureService {

  @Qualifier(value = "PictureRepository")
  private PictureRepository repository;
  private Util util;

  public PictureServiceImpl(PictureRepository repository, Util util) {
    this.repository = repository;
    this.util = util;
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
  public Optional<Picture> saveOrUpdatePicture(Picture picture, MultipartFile file) {

    File createdFile;

    try {

      if (picture.getId() != null) {

        Optional<Picture> foundPicture = this.getPictureById(picture.getId());

        // update
        if (foundPicture.isPresent()) {

          if (file != null) {

            if (deletePictureFromServer(util.getFILE_BASE_PATH() + foundPicture.get().getPath())) {

              createdFile = new File(util.getFILE_BASE_PATH() + file.getOriginalFilename());
              createdFile.createNewFile();

              file.transferTo(createdFile.getAbsoluteFile());
              foundPicture.get().setPath(file.getOriginalFilename());

              System.out.println("\nUPDATE\n");

              return Optional.of(repository.save(foundPicture.get()));
            }
          }
        }
      }

      // create
      if (file != null) {
        createdFile = new File(util.getFILE_BASE_PATH() + file.getOriginalFilename());
        createdFile.createNewFile();

        file.transferTo(createdFile.getAbsoluteFile());
        picture.setPath(file.getOriginalFilename());
      }

      System.out.println("\nCREATE\n");

      return Optional.of(repository.save(picture));

    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
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

  /**
   *
   * @param path
   * @return
   */
  private boolean deletePictureFromServer(String path) {
    File foundFile;
    foundFile = new File(path);
    return foundFile.delete();
  }
}
