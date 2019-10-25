package com.multipartfile.repositories;

import com.multipartfile.entity.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Rayner MDZ
 */
@Repository(value = "PictureRepository")
public interface PictureRepository extends JpaRepository<Picture, Integer> {
}
