package org.akaxon.crazy.task.tracker.store.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ProjectEntity {
    @Id
    private Long id;
    private String name;

}
