package fi.haagahelia.OnlineStore.web;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fi.haagahelia.OnlineStore.domain.File;
import fi.haagahelia.OnlineStore.domain.FileRepository;
import fi.haagahelia.OnlineStore.domain.Item;
import fi.haagahelia.OnlineStore.domain.ItemRepository;

@Controller
public class ItemController {
	

	@Autowired
	ItemRepository itemrepository;
	
	@Autowired
	FileRepository filerepository;
	
	@Value("${upload.path}")
    private String uploadFolder;
	
	@RequestMapping(value="/login")
	    public String login() {	
	        return "login";
	    }
	
	@RequestMapping(value= "/itemList", method=RequestMethod.GET)
	public String itemList(Model model) {
	model.addAttribute("items", itemrepository.findAll());
		return "itemlist";
	}
	
	@RequestMapping(value="/add")
	public String addItem(Model model) {
		model.addAttribute("items", new Item());
		return "additem";	
	}
	
	@RequestMapping(value="/save", method = RequestMethod.POST)
	public String saveItem(Item item) {
		itemrepository.save(item);
		return "redirect:itemList";	
	}
	
	@RequestMapping(value="/edit/{id}")
	public String editItem(@PathVariable("id") String id, Model model) {
		model.addAttribute("items", itemrepository.findById(id));
		return "edititem";	
	}
	@RequestMapping(value="/delete/{id}")
	public String deleteItem(@PathVariable("id") String id, Model model) {
		itemrepository.deleteById(id);
		return "redirect:../itemList";
	}
	
	@PostMapping("/itemList")
    public String fileUpload(@RequestParam("file") MultipartFile file, Model model, Item item) {
		  if (file.isEmpty()) {
	        	model.addAttribute("msg", "Upload failed");
	            return "uploadstatus";
	        }
        try {
            File fileModel = new File(file.getOriginalFilename(), file.getBytes());
            filerepository.save(fileModel);
            byte[] bytes = file.getBytes();
            System.out.println(filerepository.count()+"repo");
            int i=0;
            for (i=0; i<itemrepository.count(); i++) {
            	Path path_dir=Files.createDirectories(Paths.get(uploadFolder+itemrepository.findAll().get(i).getId()));
             
            	System.out.println("blabla"+itemrepository.findAll().get(i).getId());
            	System.out.println("first"+itemrepository.findAll().get(i));

            System.out.println("path_dir"+path_dir);

            Path path = Paths.get(path_dir+"/"+ file.getOriginalFilename());
            Files.write(path, bytes);
            } 
            return "redirect:/filelist";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "uploadedstatus";
    }
	
	@GetMapping("/filelist")
    public String fileList(Model model) {
    	model.addAttribute("filelist", filerepository.findAll());  	
    	return "filelist";
    }
    
	@GetMapping("/file/{id}")
	public ResponseEntity<byte[]> getFile(@PathVariable String id) {
		Optional<File> fileOptional = filerepository.findById(id);
		
		if(fileOptional.isPresent()) {
			File file = fileOptional.get();
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
					.body(file.getFile());	
		}
		
		return ResponseEntity.status(404).body(null);
	}
	@RequestMapping(value="/filedelete/{id}")
	public String deleteFile(@PathVariable("id") String id, Model model) {
		filerepository.deleteById(id);
		return "redirect:../filelist";
	}
	
	

}
