package org.akaxon.crazy.task.tracker.api.controllers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.akaxon.crazy.task.tracker.api.dto.AckDto;
import org.akaxon.crazy.task.tracker.api.dto.ProjectDto;
import org.akaxon.crazy.task.tracker.api.exceptions.BadRequestException;
import org.akaxon.crazy.task.tracker.api.exceptions.NotFoundException;
import org.akaxon.crazy.task.tracker.api.factories.ProjectDtoFactory;
import org.akaxon.crazy.task.tracker.store.entities.ProjectEntity;
import org.akaxon.crazy.task.tracker.store.repositories.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class ProjectController {

    ProjectRepository projectRepository;
    ProjectDtoFactory projectDtoFactory;

    public static final String FETCH_PROJECT = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";

    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetchProjects(@RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName){

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAll);

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());

    }

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam String name) {

        if (name.trim().isEmpty()){
            throw  new BadRequestException("Name can't be empty.");
        }

        projectRepository
                .findByName(name)
                .ifPresent( project -> {
                    throw  new BadRequestException(String.format("Project \"%s\" already exists.", name));
                });

        ProjectEntity project = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(name)
                        .build()
        );

        return projectDtoFactory.makeProjectDto(project);

    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName
            //Another params
            ) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreated = !optionalProjectId.isPresent();

        final ProjectEntity project = optionalProjectId
                .map((id) ->projectRepository
                        .findById(id)
                        .orElseThrow( () ->
                                new BadRequestException(String.format("Project \"%s\" doesn't exists.", id))
                        ))
                .orElseGet(() -> ProjectEntity.builder().build());

        if (isCreated && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name can't be empty");
        }

        optionalProjectName
                .ifPresent( projectName -> {
                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(
                                        String.format("Project \"%s\" already exists.", projectName)
                                );
                            });

                    project.setName(projectName);
                });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(savedProject);
    }


    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editPatch(
            @PathVariable("project_id") Long projectId,
            @RequestParam String name) {


        if (name.trim().isEmpty()){
            throw  new BadRequestException("Name can't be empty.");
        }

        ProjectEntity project = projectRepository
                .findById(projectId)
                        .orElseThrow(() ->
                                new NotFoundException(String.format("Project with \"%s\" doesn't exist.", projectId))
                        );

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent( anotherProject -> {
                    throw  new BadRequestException(String.format("Project \"%s\" already exists.", name));
                });

        project.setName(name);

        project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);

    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDto deleteProject(@PathVariable("project_id") Long projectId) {

        projectRepository
                .findById(projectId)
                .orElseThrow( () ->
                        new BadRequestException(String.format("Project \"%s\" doesn't exists.", projectId))
                );

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);
    }

}
