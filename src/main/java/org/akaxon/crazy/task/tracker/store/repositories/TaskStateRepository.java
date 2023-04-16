package org.akaxon.crazy.task.tracker.store.repositories;

import org.akaxon.crazy.task.tracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
}
