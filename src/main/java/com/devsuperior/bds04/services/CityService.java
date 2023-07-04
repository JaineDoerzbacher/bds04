package com.devsuperior.bds04.services;
import com.devsuperior.bds04.dto.CityDTO;
import com.devsuperior.bds04.dto.EventDTO;
import com.devsuperior.bds04.entities.City;
import com.devsuperior.bds04.entities.Event;
import com.devsuperior.bds04.repositories.CityRepository;
import com.devsuperior.bds04.repositories.EventRepository;
import com.devsuperior.bds04.services.exceptions.DatabaseException;
import com.devsuperior.bds04.services.exceptions.ResourseNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    @Autowired
    private CityRepository repository;
    @Autowired
    private EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<CityDTO> findAll() {
       List<City> list = repository.findAll(Sort.by("name"));
         return list.stream().map(CityDTO::new).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public CityDTO insert(CityDTO dto) {
        City entity = new City();
        entity.setName(dto.getName());
        entity = repository.save(entity);
        return new CityDTO(entity);
    }


    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) { // Para capturar o erro caso o id não exista
            throw new ResourseNotFoundException("Id not found " + id); // Para retornar o erro 404
        } catch (DataIntegrityViolationException e) { // Para capturar o erro caso o id esteja sendo usado por outra tabela
            throw new DatabaseException("Integrity violation");
        }
    }

    public CityDTO findById(Long id) {
        City entity = repository.findById(id).orElseThrow(() -> new ResourseNotFoundException("Entity not found"));
        return new CityDTO(entity);
    }

    @Transactional
    public CityDTO update(Long id, CityDTO dto) {
        try {
            City entity = repository.getOne(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new CityDTO(entity);
        } catch(EntityNotFoundException e) {
            throw new ResourseNotFoundException("Id not found: " + id);
        }
    }
    private void copyDtoToEntity(CityDTO dto, City entity) {
        entity.setName(dto.getName());

        entity.getEvents().clear();
        for(EventDTO eventsDto : dto.getEvents()) {
            Event event = eventRepository.getOne(eventsDto.getId());
            entity.getEvents().add(event);
        }
    }
}
