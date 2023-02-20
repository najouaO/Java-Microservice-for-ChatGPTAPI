package de.bsi.openai.chatgpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import de.bsi.openai.FormInputDTO;
import de.bsi.openai.OpenAiApiClient;
import de.bsi.openai.OpenAiApiClient.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.FileWriter;
import java.util.Arrays;

@Controller
public class ChatGptController {

	private final Logger log = LoggerFactory.getLogger(ChatGptController.class);

	private static final String MAIN_PAGE = "index";

	@Autowired
	private ObjectMapper jsonMapper;
	@Autowired
	private OpenAiApiClient client;

	private String chatWithGpt3(String message) throws Exception {
		var completion = CompletionRequest.defaultWith(message);
		var postBodyJson = jsonMapper.writeValueAsString(completion);
		var responseBody = client.postToOpenAiApi(postBodyJson, OpenAiService.GPT_3);
		var completionResponse = jsonMapper.readValue(responseBody, CompletionResponse.class);
		String response = completionResponse.firstAnswer().orElseThrow();

		String[] header = {"Question", "Answer"};
		String[] data = {message, response};

		try (CSVWriter writer = new CSVWriter(new FileWriter("C:\\data\\openai-api\\src\\main\\java\\de\\bsi\\openai\\chatgpt\\file.csv", true))) {
			writer.writeNext(header);
			writer.writeNext(data);
		}

		return response;
	}

	@GetMapping(path = "/")
	public String index() {
		return MAIN_PAGE;
	}

	@PostMapping(path = "/")
	public String chat(Model model, @ModelAttribute FormInputDTO dto) {
		try {
			log.debug("data comming = " + dto.prompt());
			model.addAttribute("request", dto.prompt());
			model.addAttribute("response", chatWithGpt3(dto.prompt()));
			log.debug("request =>" + model.getAttribute("request")
					+ "response =>" + model.getAttribute("response"));
		} catch (Exception e) {
			log.error("Error = " + e.getMessage() + "request =>" + model.getAttribute("request"));
			model.addAttribute("response", "Error in communication with OpenAI ChatGPT API.");
		}
		return MAIN_PAGE;
	}
}
