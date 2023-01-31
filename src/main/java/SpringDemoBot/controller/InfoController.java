package SpringDemoBot.controller;

import java.util.*;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SpringDemoBot.exception.ResourceNotFoundException;
import SpringDemoBot.model.Info;
import SpringDemoBot.repository.InfoRepository;

@RestController
@RequestMapping()
public class InfoController {
	@Autowired
	private InfoRepository infoRepository;

	@GetMapping("/info")
	public List<Info> getAllInfo() {
		return infoRepository.findAll();
	}

	@GetMapping("/info/{id}")
	public ResponseEntity<Info> getInfoById(@PathVariable(value = "id") Long infoId)
			throws ResourceNotFoundException {
		Info info = infoRepository.findById(infoId)
				.orElseThrow(() -> new ResourceNotFoundException("Info not found for this id : " + infoId));
		return ResponseEntity.ok().body(info);
	}


	@PostMapping("/info")
	public Info createInfo(@Valid @RequestBody Info info) {
		return infoRepository.save(info);
	}

	@PutMapping("/info/{id}")
	public ResponseEntity<Info> updateInfo(@PathVariable(value = "id") Long desmondId,
											   @Valid @RequestBody Info infoDetails) throws ResourceNotFoundException {
		Info info = infoRepository.findById(desmondId)
				.orElseThrow(() -> new ResourceNotFoundException("Desmond not found for this id : " + desmondId));

		info.setTelegramId(infoDetails.getTelegramId());
		info.setCity(infoDetails.getCity());


		final Info updatedInfo = infoRepository.save(info);
		return ResponseEntity.ok(updatedInfo);
	}

	@DeleteMapping("/info/{id}")
	public Map<String, Boolean> deleteInfo(@PathVariable(value = "id") Long desmondId)
			throws ResourceNotFoundException {
		Info info = infoRepository.findById(desmondId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for this id : " + desmondId));

		infoRepository.delete(info);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}
}
