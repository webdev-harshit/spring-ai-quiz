package com.example.demo.controller;

import java.util.List;
import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.Question;

import jakarta.servlet.http.HttpSession;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Controller
public class HomeController {
	private final ChatClient chatClient;

	public HomeController(ChatClient.Builder chatClient) {
		this.chatClient = chatClient.build();
	}
	
	@GetMapping
	public String show(HttpSession session,ModelMap model)
	{
		String s1="""
				Output ONLY a valid raw JSON array.Do not include any conversational text,
				Markdown formatting , intro or outro.
				
				Generate 5 random java true/false declarative statements
				CRICTAL:Do not generate questions starting with "what","why","how","which" or "is".
				Every item must be a fact based statement.
				
				 Each JSON object must have exactely these keys:
				 1."qno"(integer:sequential number 1 to 5)
				 2."question"(the Statement string)
				 3."answer"(boolean:true/false)
				 
				  Strict format example:
				  [
				  {
				  "qno":1,
				  "question":"an interface in java can have a private methods since java 9",
				  "answer":true
				  }
				  ] 
				   """;
		String s2=chatClient.prompt(s1).call().content();
		ObjectMapper mapper=new ObjectMapper();
		List<Question> list=mapper.readValue(s2,new TypeReference<List<Question>>() {});
		session.setAttribute("list", list);
		session.setAttribute("count", 0);
		model.addAttribute("current_question",list.get(0));
		return "question";
	}
	@GetMapping("/next")
	public String nextQuestion(HttpSession session,ModelMap model,@RequestParam("userAns") boolean userAns)
	{
		List<Question> list=(List<Question>) session.getAttribute("list");
		int count=(int)session.getAttribute("count");
		list.get(count).setUserAns(userAns);
		if(userAns==list.get(count).isAnswer())
		{
			list.get(count).setMarks(1);
		}
		if(count==4)
		{
			int c=0;
			for(Question q:list)
			{
				c=c+q.getMarks();
			}
			model.addAttribute("total",c);
			return "result";
		}
		session.setAttribute("count", ++count);
		System.out.println("userans=>"+userAns+"\tcorrectans=>"+list.get(count).isAnswer());
		model.addAttribute("current_question",list.get(count));
		return "question";
	}

}
