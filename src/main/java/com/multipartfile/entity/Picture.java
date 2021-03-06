package com.multipartfile.entity;

import com.multipartfile.enums.UploadMethods;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Project Lombok added to reduce verbosity.
 * @author Rayner MDZ
 */
@Data
@Entity
@Table(name = "picture", indexes = @Index(name = "id", columnList = "id"))
public class Picture implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "name")
  private String name;

  @Column(name = "picture_path")
  private String path;

  @Column(name = "url")
  private String url;

  @Lob
  @Column(name = "blob")
  private byte[] blob;

  @Column(name = "uri")
  private String uri;

  @Lob
  @Column(name = "picture_string")
  private String pictureString;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "upload_method")
  private UploadMethods uploadMethods;
}
