package org.akaxon.crazy.task.tracker.store.repositories;

import org.akaxon.crazy.task.tracker.store.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
}
