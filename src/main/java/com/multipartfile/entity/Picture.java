package com.multipartfile.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Project Lombok added to reduce verbosity.
 * @author Rayner MDZ
 */
@Data
@Entity
@Table(name = "picture", indexes = @Index(name = "id", columnList = "id"))
public class Picture {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "name")
  private String name;

  @Column(name = "picture_path")
  private String path;
}
